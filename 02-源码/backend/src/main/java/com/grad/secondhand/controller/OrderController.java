package com.grad.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.PageResult;
import com.grad.secondhand.common.R;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.Review;
import com.grad.secondhand.entity.TradeOrder;
import com.grad.secondhand.mapper.ReviewMapper;
import com.grad.secondhand.mapper.TradeOrderMapper;
import com.grad.secondhand.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 交易订单接口：下单、卖家确认、买家收货、取消、纠纷、评价与列表查询。
 * 状态流转全部经 TradeService 校验，禁止前端直接提交 status。
 */
@RestController
@RequestMapping("/api/trade-order")
@RequiredArgsConstructor
public class OrderController {

    private final TradeOrderMapper orderMapper;
    private final ReviewMapper reviewMapper;
    private final TradeService tradeService;

    /** 分页：学生只看本人（买家或卖家）订单，运营与管理员看全域 */
    @GetMapping("/page")
    public R<PageResult<TradeOrder>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam Map<String, String> params) {
        QueryWrapper<TradeOrder> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        if (params.get("status") != null && !params.get("status").isBlank()) {
            w.eq("status", params.get("status"));
        }
        if (params.get("keyword") != null && !params.get("keyword").isBlank()) {
            w.like("order_no", params.get("keyword").trim());
        }
        if (params.get("start_time") != null && !params.get("start_time").isBlank()) {
            w.ge("create_time", params.get("start_time"));
        }
        if (params.get("end_time") != null && !params.get("end_time").isBlank()) {
            w.le("create_time", params.get("end_time"));
        }
        if ("buyer".equals(params.get("side"))) {
            w.eq("buyer_id", UserContext.userId());
        } else if ("seller".equals(params.get("side"))) {
            w.eq("seller_id", UserContext.userId());
        } else if (UserContext.isStudent()) {
            Long uid = UserContext.userId();
            w.and(x -> x.eq("buyer_id", uid).or().eq("seller_id", uid));
        }
        w.orderByDesc("create_time");
        IPage<TradeOrder> p = orderMapper.selectPage(
                new Page<>(Math.max(1, page), Math.min(100, Math.max(1, size))), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /** 详情：主表字段 + 双方可执行操作 + 是否已评价 */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        TradeOrder o = orderMapper.selectById(id);
        if (o == null) {
            throw new BizException(4000, "订单不存在或已删除");
        }
        Long uid = UserContext.userId();
        boolean isBuyer = Objects.equals(o.getBuyerId(), uid);
        boolean isSeller = Objects.equals(o.getSellerId(), uid);
        List<String> actions = new ArrayList<>();
        switch (o.getStatus()) {
            case "待卖家确认" -> {
                if (isSeller) {
                    actions.add("confirm");
                }
                if (isBuyer) {
                    actions.add("cancel");
                }
            }
            case "待面交" -> {
                if (isBuyer) {
                    actions.add("complete");
                }
                actions.add("dispute");
                if (isSeller) {
                    actions.add("cancel");
                }
            }
            case "已完成" -> actions.add("review");
            case "纠纷中" -> actions.add("wait");
            default -> actions.add("resubmit");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order", o);
        data.put("isBuyer", isBuyer);
        data.put("isSeller", isSeller);
        data.put("actions", actions);
        data.put("reviewCount", reviewMapper.selectCount(
                new QueryWrapper<Review>().eq("order_id", id)));
        data.put("reviews", reviewMapper.selectList(new QueryWrapper<Review>()
                .eq("order_id", id).eq("status", "已展示")));
        return R.ok(data);
    }

    /** 下单：面交时间与面交点必填 */
    @PostMapping
    public R<Map<String, Object>> create(@RequestParam Long goodsId,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime appointTime,
                                         @RequestParam String appointPlace,
                                         @RequestParam(required = false) String payType) {
        return R.ok(tradeService.create(goodsId, appointTime, appointPlace, payType));
    }

    /** 卖家确认接单并填写成交价 */
    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id, @RequestBody @jakarta.validation.Valid Dtos.OrderConfirmReq req) {
        tradeService.confirm(id, req);
        return R.ok();
    }

    /** 买家确认收货 */
    @PostMapping("/{id}/complete")
    public R<Void> complete(@PathVariable Long id) {
        tradeService.complete(id);
        return R.ok();
    }

    /** 取消订单：必须填写原因 */
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id, @RequestBody Dtos.FlowReq req) {
        tradeService.cancel(id, req == null ? null : req.getReason());
        return R.ok();
    }

    /** 发起纠纷：可上传证据图，自动生成待受理举报单 */
    @PostMapping("/{id}/dispute")
    public R<Map<String, Object>> dispute(@PathVariable Long id, @RequestBody Dtos.ActionReq req) {
        String evidence = req.attachments() == null ? null : String.join(",", req.attachments());
        return R.ok(tradeService.dispute(id, req.reason(), evidence));
    }

    /** 提交评价（双盲） */
    @PostMapping("/{id}/review")
    public R<Map<String, Object>> review(@PathVariable Long id, @RequestBody @jakarta.validation.Valid Dtos.ReviewReq req) {
        Dtos.ReviewReq fixed = new Dtos.ReviewReq(id, req.score(), req.tags(), req.content(), req.anonymous());
        return R.ok(tradeService.review(fixed));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw new BizException(4030, "只有平台管理员可以删除订单");
        }
        orderMapper.deleteById(id);
        return R.ok();
    }
}
