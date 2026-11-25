package com.grad.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.Category;
import com.grad.secondhand.entity.Goods;
import com.grad.secondhand.entity.SysNotice;
import com.grad.secondhand.entity.SysUser;
import com.grad.secondhand.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 闲置商品业务：发布校验、运营审核、下架、门户检索。
 * 状态机：待审核 →（通过）在售 →（下单锁定）→ 已售出；在售 → 已下架；待审核 → 审核驳回；
 * 任意状态 →（运营冻结）冻结。状态只能通过本服务的方法变更，前端提交 status 一律忽略。
 */
@Service
@RequiredArgsConstructor
public class GoodsService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /** 标题/描述中禁止出现的联系方式（手机号、QQ、微信） */
    private static final Pattern CONTACT = Pattern.compile("(1[3-9]\\d{9})|(QQ|qq|微信|vx|VX)\\s*[:：]?\\s*\\w{5,}");

    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final SysUserMapper userMapper;
    private final TradeOrderMapper orderMapper;
    private final SysNoticeMapper noticeMapper;
    private final TraceService trace;

    /** 发布闲置：信用分与 24 小时发布量限制、联系方式拦截、分类只能选二级 */
    @Transactional
    public Map<String, Object> publish(Dtos.GoodsReq req, Long sellerId) {
        SysUser seller = userMapper.selectById(sellerId);
        if (seller == null) {
            throw new BizException(4000, "卖家账号不存在");
        }
        int credit = seller.getCreditScore() == null ? 100 : seller.getCreditScore();
        // 需求 5.4：信用分低于 40 禁止发布新商品 30 天
        if (credit < 40) {
            throw new BizException(4001, "信用分 " + credit + " 低于 40，已被限制发布新商品 30 天");
        }
        LocalDateTime dayAgo = LocalDateTime.now().minusHours(24);
        long todayCount = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getSellerId, sellerId)
                .ge(Goods::getCreateTime, dayAgo.format(FMT)));
        if (todayCount >= 10) {
            throw new BizException(4009, "同一卖家 24 小时内最多发布 10 件，请明天再试");
        }
        // 信用分低于 60：同时在线商品限制 3 件
        if (credit < 60) {
            long online = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                    .eq(Goods::getSellerId, sellerId).in(Goods::getStatus, List.of("在售", "待审核")));
            if (online >= 3) {
                throw new BizException(4001, "信用分 " + credit + " 低于 60，同时在线商品最多 3 件");
            }
        }
        if (CONTACT.matcher(req.title()).find() || CONTACT.matcher(req.description()).find()) {
            throw new BizException(4000, "标题或描述中不允许出现手机号、微信、QQ 等联系方式");
        }
        Category cat = categoryMapper.selectById(req.categoryId());
        if (cat == null || cat.getParentId() == null || cat.getParentId() == 0) {
            throw new BizException(4000, "只能选择二级分类");
        }
        if (cat.getStatus() != null && cat.getStatus() == 0) {
            throw new BizException(4001, "该分类已停用，请选择其他分类");
        }
        // 重复度检测：同卖家近 7 天标题完全一致时转人工复核并提示
        long dup = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getSellerId, sellerId).eq(Goods::getTitle, req.title().trim())
                .ge(Goods::getCreateTime, LocalDateTime.now().minusDays(7).format(FMT)));
        List<String> images = req.imageUrls();
        if (images.size() > 6) {
            throw new BizException(4000, "实拍图最多 6 张");
        }
        Goods g = new Goods();
        g.setGoodsNo(nextGoodsNo());
        g.setSellerId(sellerId);
        g.setCategoryId(req.categoryId());
        g.setTitle(req.title().trim());
        g.setDescription(req.description().trim());
        g.setConditionLevel(req.conditionLevel());
        g.setPrice(req.price());
        g.setOriginalPrice(req.originalPrice());
        g.setCampusId(req.campusId());
        g.setCampusName(seller.getCampusName());
        g.setTradePlaceId(req.tradePlaceId());
        g.setTradeType(req.tradeType() == null ? "面交" : req.tradeType());
        g.setStatus("待审核");
        g.setAuditRemark(dup > 0 ? "标题与近 7 天发布重复，转人工重点审核" : null);
        g.setViewCount(0);
        g.setFavoriteCount(0);
        g.setCoverUrl(images.get(0));
        g.setImageUrls(String.join(",", images));
        g.setCreateBy(sellerId);
        g.setCreateTime(LocalDateTime.now().format(FMT));
        g.setDeleted(0);
        g.setVersion(0);
        goodsMapper.insert(g);
        trace.log("闲置发布", "发布", g.getGoodsNo(), "标题 " + g.getTitle() + "，售价 " + g.getPrice());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", g.getId());
        data.put("goodsNo", g.getGoodsNo());
        data.put("status", g.getStatus());
        data.put("duplicateHint", dup > 0 ? "标题与近期发布重复，已转人工复核" : null);
        return data;
    }

    /** 商品编号：XY + yyMMdd + 当日 4 位序号 */
    private String nextGoodsNo() {
        String day = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long today = goodsMapper.selectCount(Wrappers.<Goods>lambdaQuery()
                .likeRight(Goods::getGoodsNo, "XY" + day));
        return "XY" + day + String.format("%04d", today + 1);
    }

    /**
     * 运营审核：通过上架、驳回必须选择原因并填写不少于 5 字说明、可同时冻结。
     * 只有待审核状态可审核，重复审核返回 4001（避免两个运营重复处理同一件）。
     */
    @Transactional
    public Map<String, Object> audit(Long id, Dtos.AuditReq req) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有校园运营人员可以审核商品");
        }
        Goods g = goodsMapper.selectById(id);
        if (g == null) {
            throw new BizException(4000, "商品不存在或已删除");
        }
        if (!"待审核".equals(g.getStatus())) {
            throw new BizException(4001, "当前状态为「" + g.getStatus() + "」，不能再次审核");
        }
        String target;
        if (Boolean.TRUE.equals(req.freeze())) {
            target = "冻结";
        } else if (Boolean.TRUE.equals(req.pass())) {
            target = "在售";
        } else {
            target = "审核驳回";
            if (req.reason() == null || req.reason().trim().length() < 5) {
                throw new BizException(4000, "驳回必须选择原因并填写不少于 5 字的说明");
            }
        }
        Goods u = new Goods();
        u.setId(id);
        u.setStatus(target);
        u.setAuditRemark(req.reason());
        u.setUpdateBy(UserContext.userId());
        u.setUpdateTime(LocalDateTime.now().format(FMT));
        goodsMapper.updateById(u);
        trace.log("商品审核", target, g.getGoodsNo(),
                "审核结果 " + target + (req.reason() == null ? "" : "，意见：" + req.reason()));
        trace.notify(g.getSellerId(), "商品审核结果：" + target,
                "你发布的《" + g.getTitle() + "》审核结果为「" + target + "」"
                        + (req.reason() == null ? "" : "，审核意见：" + req.reason()),
                "goods", id, "审核");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("status", target);
        return data;
    }

    /** 卖家下架在售商品；已售出商品不可修改价格也不可下架 */
    @Transactional
    public void offShelf(Long id, String reason) {
        Goods g = goodsMapper.selectById(id);
        if (g == null) {
            throw new BizException(4000, "商品不存在");
        }
        if (!Objects.equals(g.getSellerId(), UserContext.userId()) && !UserContext.isOperator()) {
            throw new BizException(4030, "只能下架本人发布的商品");
        }
        if (!"在售".equals(g.getStatus())) {
            throw new BizException(4001, "只有「在售」商品可以下架，当前为「" + g.getStatus() + "」");
        }
        long locked = orderMapper.selectCount(Wrappers.<com.grad.secondhand.entity.TradeOrder>lambdaQuery()
                .eq(com.grad.secondhand.entity.TradeOrder::getGoodsId, id)
                .in(com.grad.secondhand.entity.TradeOrder::getStatus, List.of("待卖家确认", "待面交", "纠纷中")));
        if (locked > 0) {
            throw new BizException(4001, "该商品有进行中的订单，请先处理订单再下架");
        }
        Goods u = new Goods();
        u.setId(id);
        u.setStatus("已下架");
        u.setAuditRemark(reason);
        goodsMapper.updateById(u);
        trace.log("商品管理", "下架", g.getGoodsNo(), "卖家主动下架：" + (reason == null ? "无原因" : reason));
    }

    /** 编辑：已售出商品不可改价；编辑后重新进入待审核 */
    @Transactional
    public void update(Long id, Dtos.GoodsReq req) {
        Goods g = goodsMapper.selectById(id);
        if (g == null) {
            throw new BizException(4000, "商品不存在");
        }
        if (!Objects.equals(g.getSellerId(), UserContext.userId())) {
            throw new BizException(4030, "只能编辑本人发布的商品");
        }
        if ("已售出".equals(g.getStatus())) {
            throw new BizException(4001, "已售出商品不可修改价格与描述");
        }
        if ("冻结".equals(g.getStatus())) {
            throw new BizException(4001, "商品处于冻结状态，申诉解冻后才能编辑");
        }
        Goods u = new Goods();
        u.setId(id);
        u.setCategoryId(req.categoryId());
        u.setTitle(req.title().trim());
        u.setDescription(req.description().trim());
        u.setConditionLevel(req.conditionLevel());
        u.setPrice(req.price());
        u.setOriginalPrice(req.originalPrice());
        u.setTradePlaceId(req.tradePlaceId());
        u.setTradeType(req.tradeType());
        u.setCoverUrl(req.imageUrls().get(0));
        u.setImageUrls(String.join(",", req.imageUrls()));
        u.setStatus("待审核");
        u.setAuditRemark(null);
        goodsMapper.updateById(u);
        trace.log("闲置发布", "编辑", g.getGoodsNo(), "编辑后重新提交审核");
    }

    /** 门户检索：只返回在售（可通过 status 指定本人可见状态），支持排序 */
    public QueryWrapper<Goods> portalWrapper(Map<String, String> params, Long viewerId) {
        QueryWrapper<Goods> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        String status = params.get("status");
        w.eq("status", status == null || status.isBlank() ? "在售" : status);
        if (params.get("keyword") != null && !params.get("keyword").isBlank()) {
            String k = params.get("keyword").trim();
            w.and(x -> x.like("title", k).or().like("description", k));
        }
        if (params.get("category_id") != null && !params.get("category_id").isBlank()) {
            w.eq("category_id", params.get("category_id"));
        }
        if (params.get("parent_id") != null && !params.get("parent_id").isBlank()) {
            w.inSql("category_id", "select id from category where parent_id = " + params.get("parent_id"));
        }
        if (params.get("campus_id") != null && !params.get("campus_id").isBlank()) {
            w.eq("campus_id", params.get("campus_id"));
        }
        if (params.get("condition_level") != null && !params.get("condition_level").isBlank()) {
            w.eq("condition_level", params.get("condition_level"));
        }
        if (params.get("min_price") != null && !params.get("min_price").isBlank()) {
            w.ge("price", params.get("min_price"));
        }
        if (params.get("max_price") != null && !params.get("max_price").isBlank()) {
            w.le("price", params.get("max_price"));
        }
        String sort = params.getOrDefault("sort", "new");
        switch (sort) {
            case "price_asc" -> w.orderByAsc("price");
            case "credit" -> w.orderByDesc("(select credit_score from sys_user u where u.id = goods.seller_id)");
            default -> w.orderByDesc("create_time");
        }
        return w;
    }

    /** 详情：浏览量 +1（同一用户 10 分钟内的重复访问由前端节流，这里只做累加） */
    @Transactional
    public Map<String, Object> detail(Long id) {
        Goods g = goodsMapper.selectById(id);
        if (g == null) {
            throw new BizException(4000, "商品不存在或已下架");
        }
        Goods u = new Goods();
        u.setId(id);
        u.setViewCount((g.getViewCount() == null ? 0 : g.getViewCount()) + 1);
        goodsMapper.updateById(u);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("goods", g);
        SysUser seller = userMapper.selectById(g.getSellerId());
        if (seller != null) {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("id", seller.getId());
            s.put("realName", seller.getRealName());
            s.put("avatar", seller.getAvatar());
            s.put("creditScore", seller.getCreditScore());
            s.put("campusName", seller.getCampusName());
            s.put("dealCount", orderMapper.selectCount(Wrappers.<com.grad.secondhand.entity.TradeOrder>lambdaQuery()
                    .eq(com.grad.secondhand.entity.TradeOrder::getSellerId, seller.getId())
                    .eq(com.grad.secondhand.entity.TradeOrder::getStatus, "已完成")));
            m.put("seller", s);
        }
        m.put("discountRate", g.getOriginalPrice() == null || g.getOriginalPrice().compareTo(BigDecimal.ZERO) == 0
                ? null
                : BigDecimal.ONE.subtract(g.getPrice().divide(g.getOriginalPrice(), 4, RoundingMode.HALF_UP))
                .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP));
        m.put("notices", noticeMapper.selectList(Wrappers.<SysNotice>lambdaQuery()
                .eq(SysNotice::getStatus, "已发布").orderByDesc(SysNotice::getPublishTime).last("limit 3")));
        return m;
    }
}
