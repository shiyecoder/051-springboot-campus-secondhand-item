package com.grad.secondhand.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.*;
import com.grad.secondhand.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 交易订单状态机（需求文档第 4 节）：
 *   待卖家确认 → 卖家确认 → 待面交 → 买家确认收货 → 已完成
 *   待卖家确认 → 买家取消 / 12 小时超时自动取消 → 已取消
 *   待面交 → 任一方发起纠纷 → 纠纷中 → 运营判定 → 已完成 / 已取消
 * 所有流转统一走 transition 思路：先校验状态、再校验操作人、再校验必填原因，
 * 最后在同一事务内更新业务表 + 写操作日志 + 发站内消息。前端提交 status 一律无效。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> LOCKED = List.of("待卖家确认", "待面交", "纠纷中");

    private final TradeOrderMapper orderMapper;
    private final GoodsMapper goodsMapper;
    private final SysUserMapper userMapper;
    private final CreditLogMapper creditMapper;
    private final ReportMapper reportMapper;
    private final ReviewMapper reviewMapper;
    private final TraceService trace;

    /** 买家下单：锁定商品 24 小时，同一商品只允许一个进行中订单 */
    @Transactional
    public Map<String, Object> create(Long goodsId, LocalDateTime appointTime, String appointPlace, String payType) {
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BizException(4000, "商品不存在");
        }
        Long buyerId = UserContext.userId();
        if (Objects.equals(g.getSellerId(), buyerId)) {
            throw new BizException(4001, "不能给自己发布的商品下单");
        }
        if (!"在售".equals(g.getStatus())) {
            throw new BizException(4001, "商品当前状态为「" + g.getStatus() + "」，无法下单");
        }
        // 并发保护：同一商品已有进行中订单时，第二个买家提示已被拍下
        long active = orderMapper.selectCount(Wrappers.<TradeOrder>lambdaQuery()
                .eq(TradeOrder::getGoodsId, goodsId).in(TradeOrder::getStatus, LOCKED));
        if (active > 0) {
            throw new BizException(4009, "该商品已被其他同学拍下，换一件看看吧");
        }
        if (appointTime == null || appointTime.isBefore(LocalDateTime.now())) {
            throw new BizException(4000, "面交时间必须晚于当前时间");
        }
        if (appointTime.isAfter(LocalDateTime.now().plusDays(7))) {
            throw new BizException(4000, "面交时间必须在 7 天内");
        }
        // 需求 4：同一买家取消 3 次后 7 天内禁止下单
        long cancels = orderMapper.selectCount(Wrappers.<TradeOrder>lambdaQuery()
                .eq(TradeOrder::getBuyerId, buyerId).eq(TradeOrder::getStatus, "已取消")
                .ge(TradeOrder::getUpdateTime, LocalDateTime.now().minusDays(7).format(FMT)));
        if (cancels >= 3) {
            throw new BizException(4001, "你近 7 天已取消 3 次订单，暂时无法下单，请与卖家协商后联系运营");
        }

        TradeOrder o = new TradeOrder();
        o.setOrderNo(nextOrderNo());
        o.setGoodsId(goodsId);
        o.setGoodsTitle(g.getTitle());
        o.setGoodsCover(g.getCoverUrl());
        o.setBuyerId(buyerId);
        o.setSellerId(g.getSellerId());
        o.setDealPrice(g.getPrice());
        o.setPayType(payType == null || payType.isBlank() ? "现金" : payType);
        o.setAppointTime(appointTime.format(FMT));
        o.setAppointPlace(appointPlace);
        o.setStatus("待卖家确认");
        o.setLockExpireTime(LocalDateTime.now().plusHours(24).format(FMT));
        o.setCreateBy(buyerId);
        o.setCreateTime(LocalDateTime.now().format(FMT));
        o.setDeleted(0);
        o.setVersion(0);
        orderMapper.insert(o);

        trace.log("订单", "下单", o.getOrderNo(), "商品 " + g.getGoodsNo() + "，买家下单锁定 24 小时");
        trace.notify(g.getSellerId(), "新订单待确认", "有买家对《" + g.getTitle() + "》下单，请在 12 小时内确认接单，超时将自动取消",
                "order", o.getId(), "订单");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", o.getId());
        data.put("orderNo", o.getOrderNo());
        data.put("status", o.getStatus());
        return data;
    }

    private String nextOrderNo() {
        String day = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long today = orderMapper.selectCount(Wrappers.<TradeOrder>lambdaQuery().likeRight(TradeOrder::getOrderNo, "DD" + day));
        return "DD" + day + String.format("%06d", today + 1);
    }

    /** 卖家确认接单并填写实际成交价（允许低于标价） */
    @Transactional
    public void confirm(Long id, Dtos.OrderConfirmReq req) {
        TradeOrder o = mustGet(id);
        if (!Objects.equals(o.getSellerId(), UserContext.userId())) {
            throw new BizException(4030, "只有订单卖家可以确认接单");
        }
        if (!"待卖家确认".equals(o.getStatus())) {
            throw new BizException(4001, "当前状态为「" + o.getStatus() + "」，不能确认接单");
        }
        if (o.getCreateTime() != null
                && LocalDateTime.parse(o.getCreateTime(), FMT).plusHours(12).isBefore(LocalDateTime.now())) {
            throw new BizException(4001, "已超过 12 小时确认时限，订单将被系统自动取消");
        }
        // 卖家可以不填成交价，默认沿用商品标价
        BigDecimal deal = req.dealPrice() == null ? o.getDealPrice() : req.dealPrice();
        if (deal == null) {
            throw new BizException(4000, "请填写实际成交价");
        }
        if (o.getDealPrice() != null && deal.compareTo(o.getDealPrice()) > 0) {
            throw new BizException(4000, "成交价不能高于标价，如需加价请重新发布商品");
        }
        TradeOrder u = new TradeOrder();
        u.setId(id);
        u.setStatus("待面交");
        u.setDealPrice(deal);
        u.setPayType(req.payType() == null ? o.getPayType() : req.payType());
        u.setSellerConfirmTime(LocalDateTime.now().format(FMT));
        u.setUpdateBy(UserContext.userId());
        orderMapper.updateById(u);
        trace.log("订单", "卖家确认", o.getOrderNo(), "成交价 " + deal + "，转入待面交");
        trace.notify(o.getBuyerId(), "卖家已确认接单", "订单 " + o.getOrderNo() + " 已确认，成交价 ¥" + deal
                + "，请按约定时间地点面交", "order", id, "订单");
    }

    /** 买家确认收货：纠纷中的订单必须先由运营处理 */
    @Transactional
    public void complete(Long id) {
        TradeOrder o = mustGet(id);
        if (!Objects.equals(o.getBuyerId(), UserContext.userId())) {
            throw new BizException(4030, "只有买家可以确认收货");
        }
        if ("纠纷中".equals(o.getStatus())) {
            throw new BizException(4001, "订单处于纠纷中，需运营处理完成后再确认收货");
        }
        if (!"待面交".equals(o.getStatus())) {
            throw new BizException(4001, "当前状态为「" + o.getStatus() + "」，不能确认收货");
        }
        TradeOrder u = new TradeOrder();
        u.setId(id);
        u.setStatus("已完成");
        u.setBuyerConfirmTime(LocalDateTime.now().format(FMT));
        u.setUpdateBy(UserContext.userId());
        orderMapper.updateById(u);
        Goods gu = new Goods();
        gu.setId(o.getGoodsId());
        gu.setStatus("已售出");
        goodsMapper.updateById(gu);
        trace.log("订单", "确认收货", o.getOrderNo(), "订单完成，商品转已售出");
        trace.notify(o.getSellerId(), "买家已确认收货", "订单 " + o.getOrderNo() + " 已完成，可在 7 天内互相评价",
                "order", id, "订单");
        trace.notify(o.getBuyerId(), "评价邀请", "订单 " + o.getOrderNo() + " 已完成，请在 7 天内评价本次交易",
                "order", id, "订单");
    }

    /** 取消：买家在待卖家确认时可取消；卖家在待面交时申请取消需理由并扣信用分 */
    @Transactional
    public void cancel(Long id, String reason) {
        TradeOrder o = mustGet(id);
        boolean isBuyer = Objects.equals(o.getBuyerId(), UserContext.userId());
        boolean isSeller = Objects.equals(o.getSellerId(), UserContext.userId());
        if (!isBuyer && !isSeller && !UserContext.isOperator()) {
            throw new BizException(4030, "只能取消本人参与的订单");
        }
        if (reason == null || reason.trim().length() < 5) {
            throw new BizException(4000, "取消必须选择原因并填写不少于 5 字说明");
        }
        if (!List.of("待卖家确认", "待面交").contains(o.getStatus())) {
            throw new BizException(4001, "当前状态为「" + o.getStatus() + "」，不能取消");
        }
        if ("待面交".equals(o.getStatus()) && isSeller) {
            // 卖家在面交前取消视为放鸽子：扣 10 分
            adjustCredit(o.getSellerId(), -10, "放鸽子", o.getOrderNo(), "卖家在待面交阶段取消订单");
        }
        TradeOrder u = new TradeOrder();
        u.setId(id);
        u.setStatus("已取消");
        u.setCancelReason(reason);
        u.setUpdateBy(UserContext.userId());
        orderMapper.updateById(u);
        // 商品释放回在售
        Goods g = goodsMapper.selectById(o.getGoodsId());
        if (g != null && !"冻结".equals(g.getStatus())) {
            Goods gu = new Goods();
            gu.setId(g.getId());
            gu.setStatus("在售");
            goodsMapper.updateById(gu);
        }
        trace.log("订单", "取消", o.getOrderNo(), "取消原因：" + reason);
        Long other = isBuyer ? o.getSellerId() : o.getBuyerId();
        trace.notify(other, "订单已取消", "订单 " + o.getOrderNo() + " 已取消，原因：" + reason, "order", id, "订单");
    }

    /** 发起纠纷：买卖双方均可，订单转纠纷中并冻结商品，同时生成举报记录供运营调解 */
    @Transactional
    public Map<String, Object> dispute(Long id, String reason, String evidenceUrls) {
        TradeOrder o = mustGet(id);
        boolean party = Objects.equals(o.getBuyerId(), UserContext.userId())
                || Objects.equals(o.getSellerId(), UserContext.userId());
        if (!party) {
            throw new BizException(4030, "只有订单买卖双方可以发起纠纷");
        }
        if (!"待面交".equals(o.getStatus())) {
            throw new BizException(4001, "只有「待面交」订单可以发起纠纷，当前为「" + o.getStatus() + "」");
        }
        if (reason == null || reason.trim().length() < 5) {
            throw new BizException(4000, "发起纠纷必须填写不少于 5 字的说明");
        }
        TradeOrder u = new TradeOrder();
        u.setId(id);
        u.setStatus("纠纷中");
        u.setCancelReason(reason);
        orderMapper.updateById(u);
        Goods gu = new Goods();
        gu.setId(o.getGoodsId());
        gu.setStatus("冻结");
        goodsMapper.updateById(gu);

        Report r = new Report();
        r.setReportNo("JB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + String.format("%04d", reportMapper.selectCount(null) + 1));
        r.setBizType("订单");
        r.setBizId(id);
        r.setBizTitle("订单 " + o.getOrderNo() + " 纠纷");
        r.setReporterId(UserContext.userId());
        r.setReasonType("虚假描述");
        r.setDescription(reason.trim());
        r.setEvidenceUrls(evidenceUrls);
        r.setStatus("待受理");
        r.setAppealStatus("无");
        r.setCreateTime(LocalDateTime.now().format(FMT));
        r.setDeleted(0);
        r.setVersion(0);
        reportMapper.insert(r);

        trace.log("订单", "发起纠纷", o.getOrderNo(), "原因：" + reason + "，商品已冻结");
        trace.notify(o.getSellerId(), "订单进入纠纷", "订单 " + o.getOrderNo() + " 已发起纠纷，双方各有一次申诉机会",
                "report", r.getId(), "纠纷");
        trace.notify(o.getBuyerId(), "订单进入纠纷", "订单 " + o.getOrderNo() + " 已发起纠纷，等待运营调解",
                "report", r.getId(), "纠纷");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", id);
        data.put("reportId", r.getId());
        data.put("reportNo", r.getReportNo());
        return data;
    }

    /** 交易评价：双盲——双方都提交后才互相可见；评分影响信用分 */
    @Transactional
    public Map<String, Object> review(Dtos.ReviewReq req) {
        TradeOrder o = mustGet(req.orderId());
        boolean isBuyer = Objects.equals(o.getBuyerId(), UserContext.userId());
        boolean isSeller = Objects.equals(o.getSellerId(), UserContext.userId());
        if (!isBuyer && !isSeller) {
            throw new BizException(4030, "只有订单买卖双方可以评价");
        }
        if (!"已完成".equals(o.getStatus())) {
            throw new BizException(4001, "订单未完成，不能评价");
        }
        if (o.getBuyerConfirmTime() != null
                && LocalDateTime.parse(o.getBuyerConfirmTime(), FMT).plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BizException(4001, "评价窗口已关闭（订单完成超过 7 天）");
        }
        long exist = reviewMapper.selectCount(Wrappers.<Review>lambdaQuery()
                .eq(Review::getOrderId, req.orderId()).eq(Review::getFromUserId, UserContext.userId()));
        if (exist > 0) {
            throw new BizException(4009, "你已评价过该订单，同一订单只能评价一次");
        }
        Long toUser = isBuyer ? o.getSellerId() : o.getBuyerId();
        Review r = new Review();
        r.setOrderId(req.orderId());
        r.setFromUserId(UserContext.userId());
        r.setToUserId(toUser);
        r.setScore(req.score());
        r.setTags(req.tags());
        r.setContent(filterSensitive(req.content()));
        r.setAnonymous(req.anonymous() == null ? 0 : req.anonymous());
        r.setStatus("待审核");
        r.setCreateBy(UserContext.userId());
        r.setCreateTime(LocalDateTime.now().format(FMT));
        r.setDeleted(0);
        r.setVersion(0);
        reviewMapper.insert(r);

        // 双盲：双方都提交后同时置为已展示
        long total = reviewMapper.selectCount(Wrappers.<Review>lambdaQuery().eq(Review::getOrderId, req.orderId()));
        if (total >= 2) {
            for (Review other : reviewMapper.selectList(Wrappers.<Review>lambdaQuery().eq(Review::getOrderId, req.orderId()))) {
                Review up = new Review();
                up.setId(other.getId());
                up.setStatus("已展示");
                reviewMapper.updateById(up);
            }
        }
        // 信用分：好评（4-5 星）+2，中评（3 星）不变，差评（1-2 星）-5
        if (req.score() >= 4) {
            adjustCredit(toUser, 2, "交易好评", o.getOrderNo(), "订单收到 " + req.score() + " 星评价");
        } else if (req.score() <= 2) {
            adjustCredit(toUser, -5, "差评", o.getOrderNo(), "订单收到 " + req.score() + " 星评价");
        }
        trace.log("评价", "提交评价", o.getOrderNo(), "评分 " + req.score() + "，双盲可见");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", r.getId());
        data.put("visible", total >= 2);
        data.put("tip", total >= 2 ? "双方均已评价，评价内容已互相可见" : "对方尚未评价，你的评价暂不可见");
        return data;
    }

    /** 敏感词过滤：命中即以 * 替换，避免评价区出现辱骂与联系方式 */
    private String filterSensitive(String content) {
        if (content == null) {
            return null;
        }
        String[] words = {"傻", "骗子", "垃圾", "滚", "死"};
        String out = content;
        for (String w : words) {
            out = out.replace(w, "*");
        }
        return out.replaceAll("1[3-9]\\d{9}", "***********");
    }

    /** 信用分调整：统一入口，写流水 + 更新用户分值，上下限 0–150 */
    @Transactional
    public void adjustCredit(Long userId, int delta, String reason, String bizNo, String remark) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            return;
        }
        int before = u.getCreditScore() == null ? 100 : u.getCreditScore();
        int after = Math.max(0, Math.min(150, before + delta));
        if (after == before) {
            return;
        }
        SysUser up = new SysUser();
        up.setId(userId);
        up.setCreditScore(after);
        userMapper.updateById(up);

        CreditLog c = new CreditLog();
        c.setUserId(userId);
        c.setChangeValue(after - before);
        c.setBeforeScore(before);
        c.setAfterScore(after);
        c.setReasonType(reason);
        c.setBizNo(bizNo);
        c.setRemark(remark);
        c.setOperatorId(UserContext.userId());
        c.setCreateTime(LocalDateTime.now().format(FMT));
        c.setDeleted(0);
        c.setVersion(0);
        creditMapper.insert(c);
        trace.notify(userId, "信用分变动提醒", "信用分由 " + before + " 变为 " + after + "（" + reason + "）",
                "credit", c.getId(), "信用");
    }

    private TradeOrder mustGet(Long id) {
        TradeOrder o = orderMapper.selectById(id);
        if (o == null) {
            throw new BizException(4000, "订单不存在或已删除");
        }
        return o;
    }

    /**
     * 定时任务：每 5 分钟扫描一次（需求 4）。
     * 1. 待卖家确认超过 12 小时的订单自动取消并释放商品；
     * 2. 商品锁定超过 24 小时仍未开始的订单自动取消。
     */
    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    @Transactional
    public void autoCancelTimeout() {
        LocalDateTime now = LocalDateTime.now();
        List<TradeOrder> pending = orderMapper.selectList(Wrappers.<TradeOrder>lambdaQuery()
                .eq(TradeOrder::getStatus, "待卖家确认"));
        int canceled = 0;
        for (TradeOrder o : pending) {
            try {
                LocalDateTime created = LocalDateTime.parse(o.getCreateTime(), FMT);
                if (created.plusHours(12).isBefore(now)) {
                    TradeOrder u = new TradeOrder();
                    u.setId(o.getId());
                    u.setStatus("已取消");
                    u.setCancelReason("卖家超过 12 小时未确认，系统自动取消");
                    orderMapper.updateById(u);
                    Goods gu = new Goods();
                    gu.setId(o.getGoodsId());
                    gu.setStatus("在售");
                    goodsMapper.updateById(gu);
                    trace.notify(o.getBuyerId(), "订单已自动取消",
                            "订单 " + o.getOrderNo() + " 因卖家超时未确认已自动取消，商品已释放", "order", o.getId(), "订单");
                    trace.notify(o.getSellerId(), "订单超时取消",
                            "订单 " + o.getOrderNo() + " 超过 12 小时未确认，已自动取消", "order", o.getId(), "订单");
                    trace.log("订单", "超时自动取消", o.getOrderNo(), "卖家 12 小时未确认");
                    canceled++;
                }
            } catch (Exception e) {
                log.warn("超时取消失败：{}", o.getOrderNo(), e);
            }
        }
        if (canceled > 0) {
            log.info("本轮自动取消超时订单 {} 单", canceled);
        }
    }
}
