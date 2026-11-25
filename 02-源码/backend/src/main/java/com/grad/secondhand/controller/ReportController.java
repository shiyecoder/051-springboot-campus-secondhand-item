package com.grad.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.PageResult;
import com.grad.secondhand.common.R;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.Goods;
import com.grad.secondhand.entity.Report;
import com.grad.secondhand.entity.Review;
import com.grad.secondhand.entity.TradeOrder;
import com.grad.secondhand.mapper.GoodsMapper;
import com.grad.secondhand.mapper.ReportMapper;
import com.grad.secondhand.mapper.ReviewMapper;
import com.grad.secondhand.mapper.TradeOrderMapper;
import com.grad.secondhand.service.TraceService;
import com.grad.secondhand.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 举报与纠纷处理（需求 5.5）：
 * 待受理 → 处理中 → 已处理 / 已驳回；被举报人可申诉一次，申诉由另一名运营复核（不得自审）。
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ReportMapper reportMapper;
    private final GoodsMapper goodsMapper;
    private final TradeOrderMapper orderMapper;
    private final ReviewMapper reviewMapper;
    private final TradeService tradeService;
    private final TraceService trace;

    /** 分页：学生只看本人提交或涉及的举报，运营与管理员看全部 */
    @GetMapping("/page")
    public R<PageResult<Report>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam Map<String, String> params) {
        QueryWrapper<Report> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        if (params.get("status") != null && !params.get("status").isBlank()) {
            w.eq("status", params.get("status"));
        }
        if (params.get("reason_type") != null && !params.get("reason_type").isBlank()) {
            w.eq("reason_type", params.get("reason_type"));
        }
        if (params.get("keyword") != null && !params.get("keyword").isBlank()) {
            w.like("report_no", params.get("keyword").trim());
        }
        if (UserContext.isStudent()) {
            w.eq("reporter_id", UserContext.userId());
        }
        w.orderByDesc("create_time");
        IPage<Report> p = reportMapper.selectPage(
                new Page<>(Math.max(1, page), Math.min(100, Math.max(1, size))), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    @GetMapping("/{id}")
    public R<Report> get(@PathVariable Long id) {
        Report r = reportMapper.selectById(id);
        if (r == null) {
            throw new BizException(4000, "举报记录不存在");
        }
        return R.ok(r);
    }

    /** 提交举报：必须上传证据，描述不少于 10 字 */
    @PostMapping
    public R<Map<String, Object>> create(@RequestBody @jakarta.validation.Valid Dtos.ReportReq req) {
        if (req.evidenceUrls() == null || req.evidenceUrls().isBlank()) {
            throw new BizException(4000, "举报必须上传截图或聊天记录作为证据");
        }
        String title = "";
        if ("商品".equals(req.bizType())) {
            Goods g = goodsMapper.selectById(req.bizId());
            title = g == null ? "" : g.getTitle();
        } else if ("订单".equals(req.bizType())) {
            TradeOrder o = orderMapper.selectById(req.bizId());
            title = o == null ? "" : "订单 " + o.getOrderNo();
        }
        Report r = new Report();
        r.setReportNo("JB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + String.format("%04d", reportMapper.selectCount(null) + 1));
        r.setBizType(req.bizType());
        r.setBizId(req.bizId());
        r.setBizTitle(title);
        r.setReporterId(UserContext.userId());
        r.setReasonType(req.reasonType());
        r.setDescription(req.description());
        r.setEvidenceUrls(req.evidenceUrls());
        r.setStatus("待受理");
        r.setAppealStatus("无");
        r.setCreateBy(UserContext.userId());
        r.setCreateTime(LocalDateTime.now().format(FMT));
        r.setDeleted(0);
        r.setVersion(0);
        reportMapper.insert(r);
        trace.log("举报", "提交举报", r.getReportNo(), req.reasonType() + "：" + req.description());
        trace.notify(r.getReporterId(), "举报已提交", "举报 " + r.getReportNo() + " 已提交，运营将在 48 小时内受理",
                "report", r.getId(), "纠纷");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", r.getId());
        data.put("reportNo", r.getReportNo());
        return R.ok(data);
    }

    /**
     * 受理并处理：冻结商品、限制账号、驳回或调解结案。
     * 举报人不能处理自己的案件；已处理的记录再次提交返回 4009（幂等保护）。
     */
    @PostMapping("/{id}/handle")
    public R<Map<String, Object>> handle(@PathVariable Long id, @RequestBody @jakarta.validation.Valid Dtos.ReportHandleReq req) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有校园运营人员可以处理举报");
        }
        Report r = reportMapper.selectById(id);
        if (r == null) {
            throw new BizException(4000, "举报记录不存在");
        }
        if (Objects.equals(r.getReporterId(), UserContext.userId())) {
            throw new BizException(4030, "不得处理自己提交的举报，请由其他运营复核");
        }
        if (List.of("已处理", "已驳回").contains(r.getStatus())) {
            throw new BizException(4009, "该举报已处理完成，不可重复处理");
        }
        String target = req.targetStatus() == null ? "已处理" : req.targetStatus();
        if (!List.of("处理中", "已处理", "已驳回").contains(target)) {
            throw new BizException(4000, "处理状态只能是 处理中/已处理/已驳回");
        }
        Report u = new Report();
        u.setId(id);
        u.setStatus(target);
        u.setHandlerId(UserContext.userId());
        u.setHandlerName(UserContext.realName());
        u.setHandleResult(req.result());
        if (!"处理中".equals(target)) {
            u.setAppealStatus("无");
        }
        reportMapper.updateById(u);

        // 冻结商品
        if (Boolean.TRUE.equals(req.freezeGoods()) && "商品".equals(r.getBizType())) {
            Goods g = new Goods();
            g.setId(r.getBizId());
            g.setStatus("冻结");
            g.setAuditRemark("举报处理：商品冻结");
            goodsMapper.updateById(g);
            tradeService.adjustCredit(goodsMapper.selectById(r.getBizId()).getSellerId(), -15,
                    "违规下架", r.getReportNo(), "举报核实，商品冻结并扣 15 分");
        }
        // 订单纠纷判定：成交或取消
        if ("订单".equals(r.getBizType())) {
            TradeOrder o = orderMapper.selectById(r.getBizId());
            if (o != null && "纠纷中".equals(o.getStatus())) {
                TradeOrder ou = new TradeOrder();
                ou.setId(o.getId());
                if ("已处理".equals(target)) {
                    ou.setStatus("已取消");
                    ou.setCancelReason("运营判定取消：" + req.result());
                    Goods g = new Goods();
                    g.setId(o.getGoodsId());
                    g.setStatus("在售");
                    goodsMapper.updateById(g);
                }
                orderMapper.updateById(ou);
            }
        }
        trace.log("举报处理", target, r.getReportNo(), req.result());
        trace.notify(r.getReporterId(), "举报处理结果", "举报 " + r.getReportNo() + " 处理结果：" + req.result(),
                "report", id, "纠纷");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("status", target);
        return R.ok(data);
    }

    /** 被举报人申诉一次，转另一名运营复核 */
    @PostMapping("/{id}/appeal")
    public R<Void> appeal(@PathVariable Long id, @RequestBody Dtos.ActionReq req) {
        Report r = reportMapper.selectById(id);
        if (r == null) {
            throw new BizException(4000, "举报记录不存在");
        }
        if (!"无".equals(r.getAppealStatus())) {
            throw new BizException(4009, "每个案件只允许申诉一次");
        }
        Report u = new Report();
        u.setId(id);
        u.setAppealStatus("申诉中");
        u.setStatus("申诉中");
        reportMapper.updateById(u);
        trace.log("举报申诉", "提交申诉", r.getReportNo(), "申诉理由：" + req.reason());
        trace.notify(r.getHandlerId(), "收到申诉", "举报 " + r.getReportNo() + " 收到申诉，请转交其他运营复核",
                "report", id, "纠纷");
        return R.ok();
    }

    /** 评价管理：运营可隐藏违规评价 */
    @GetMapping("/review/page")
    public R<PageResult<Review>> reviewPage(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String status) {
        QueryWrapper<Review> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        if (status != null && !status.isBlank()) {
            w.eq("status", status);
        }
        w.orderByDesc("create_time");
        IPage<Review> p = reviewMapper.selectPage(new Page<>(page, size), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /** 评价审核：通过展示或隐藏 */
    @PutMapping("/review/{id}/status")
    public R<Void> reviewStatus(@PathVariable Long id, @RequestParam String status) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有校园运营人员可以审核评价");
        }
        if (!List.of("已展示", "已隐藏").contains(status)) {
            throw new BizException(4000, "评价状态只能是 已展示/已隐藏");
        }
        Review r = new Review();
        r.setId(id);
        r.setStatus(status);
        reviewMapper.updateById(r);
        trace.log("评价管理", status, "review:" + id, "评价置为 " + status);
        return R.ok();
    }
}
