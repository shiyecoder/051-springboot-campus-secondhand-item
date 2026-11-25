package com.grad.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grad.secondhand.common.PageResult;
import com.grad.secondhand.common.R;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.Goods;
import com.grad.secondhand.mapper.GoodsMapper;
import com.grad.secondhand.service.GoodsService;
import com.grad.secondhand.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 闲置商品接口：后台管理分页、门户检索、详情、发布、编辑、审核、下架、定价参考。
 * 写接口一律由 Service 复核角色与状态，前端隐藏按钮不作为授权依据。
 */
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsMapper goodsMapper;
    private final GoodsService goodsService;
    private final PriceService priceService;

    /** 后台分页：全量（运营/管理员），支持状态、分类、校区、卖家与时间区间筛选 */
    @GetMapping("/page")
    public R<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam Map<String, String> params) {
        QueryWrapper<Goods> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        if (notBlank(params.get("status"))) {
            w.eq("status", params.get("status"));
        }
        if (notBlank(params.get("category_id"))) {
            w.eq("category_id", params.get("category_id"));
        }
        if (notBlank(params.get("parent_id"))) {
            w.inSql("category_id", "select id from category where parent_id = " + params.get("parent_id"));
        }
        if (notBlank(params.get("campus_id"))) {
            w.eq("campus_id", params.get("campus_id"));
        }
        if (notBlank(params.get("seller_id"))) {
            w.eq("seller_id", params.get("seller_id"));
        }
        if (notBlank(params.get("condition_level"))) {
            w.eq("condition_level", params.get("condition_level"));
        }
        if (notBlank(params.get("start_time"))) {
            w.ge("create_time", params.get("start_time"));
        }
        if (notBlank(params.get("end_time"))) {
            w.le("create_time", params.get("end_time"));
        }
        if (notBlank(params.get("keyword"))) {
            String k = params.get("keyword").trim();
            w.and(x -> x.like("title", k).or().like("goods_no", k));
        }
        // 学生只看本人数据；运营与管理员看全域（数据范围三层控制）
        if (UserContext.isStudent()) {
            w.eq("seller_id", UserContext.userId());
        }
        w.orderByDesc("create_time");
        IPage<Goods> p = goodsMapper.selectPage(
                new Page<>(Math.max(1, page), Math.min(100, Math.max(1, size))), w);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Goods g : p.getRecords()) {
            rows.add(toRow(g));
        }
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), rows));
    }

    /** 门户检索：只查在售，支持关键词、两级分类、价格区间、成色、校区与三种排序 */
    @GetMapping("/search")
    public R<PageResult<Goods>> search(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "12") long size,
                                       @RequestParam Map<String, String> params) {
        IPage<Goods> p = goodsMapper.selectPage(
                new Page<>(Math.max(1, page), Math.min(100, Math.max(1, size))),
                goodsService.portalWrapper(params, UserContext.userId()));
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /** 详情：含卖家信用、成交数、折价率与风险提示 */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(goodsService.detail(id));
    }

    /** 定价参考：创新点的对外入口，预览不落库；persist=true 时写入计算记录 */
    @GetMapping("/{id}/price-reference")
    public R<Map<String, Object>> priceReference(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "false") boolean persist) {
        return R.ok(priceService.calculate(id, persist));
    }

    @PostMapping
    public R<Map<String, Object>> publish(@RequestBody @jakarta.validation.Valid Dtos.GoodsReq req) {
        return R.ok(goodsService.publish(req, UserContext.userId()));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody @jakarta.validation.Valid Dtos.GoodsReq req) {
        goodsService.update(id, req);
        return R.ok();
    }

    /** 运营审核：通过 / 驳回（必填意见）/ 冻结 */
    @PostMapping("/{id}/audit")
    public R<Map<String, Object>> audit(@PathVariable Long id, @RequestBody Dtos.AuditReq req) {
        return R.ok(goodsService.audit(id, req));
    }

    /** 卖家主动下架 */
    @PostMapping("/{id}/off-shelf")
    public R<Void> offShelf(@PathVariable Long id, @RequestBody(required = false) Dtos.FlowReq req) {
        goodsService.offShelf(id, req == null ? null : req.getReason());
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw new com.grad.secondhand.common.BizException(4030, "只有平台管理员可以删除商品");
        }
        goodsMapper.deleteById(id);
        return R.ok();
    }

    /** 列表行：补充分类名与卖家名，避免前端二次请求 */
    private Map<String, Object> toRow(Goods g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("goods_no", g.getGoodsNo());
        m.put("title", g.getTitle());
        m.put("seller_id", g.getSellerId());
        m.put("category_id", g.getCategoryId());
        m.put("condition_level", g.getConditionLevel());
        m.put("price", g.getPrice());
        m.put("original_price", g.getOriginalPrice());
        m.put("campus_name", g.getCampusName());
        m.put("trade_type", g.getTradeType());
        m.put("status", g.getStatus());
        m.put("audit_remark", g.getAuditRemark());
        m.put("view_count", g.getViewCount());
        m.put("favorite_count", g.getFavoriteCount());
        m.put("cover_url", g.getCoverUrl());
        m.put("image_urls", g.getImageUrls());
        m.put("create_time", g.getCreateTime());
        return m;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
