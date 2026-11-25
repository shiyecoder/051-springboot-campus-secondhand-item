package com.grad.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grad.secondhand.common.PageResult;
import com.grad.secondhand.common.R;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.PriceCalc;
import com.grad.secondhand.entity.PriceRule;
import com.grad.secondhand.mapper.PriceCalcMapper;
import com.grad.secondhand.mapper.PriceRuleMapper;
import com.grad.secondhand.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 创新点接口：规则配置、评分计算、计算留痕、人工修正与采纳统计。
 * 规则权重合计必须 100%，保存即生成新版本并立即生效；历史记录固化 rule_version 不受影响。
 */
@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;
    private final PriceRuleMapper ruleMapper;
    private final PriceCalcMapper calcMapper;

    /** 规则列表（含权重与状态），首页规则卡片与版本历史共用 */
    @GetMapping("/rules")
    public R<List<PriceRule>> rules() {
        return R.ok(ruleMapper.selectList(new QueryWrapper<PriceRule>()
                .eq("deleted", 0).orderByDesc("id")));
    }

    @GetMapping("/rules/active")
    public R<PriceRule> activeRule() {
        return R.ok(priceService.activeRule());
    }

    /** 保存新版本规则（仅管理员与运营） */
    @PostMapping("/rules")
    public R<Map<String, Object>> saveRule(@RequestBody @jakarta.validation.Valid Dtos.RuleReq req) {
        if (!UserContext.isOperator()) {
            throw new com.grad.secondhand.common.BizException(4030, "只有运营人员可以修改定价规则");
        }
        return R.ok(priceService.saveRule(req));
    }

    /** 计算记录分页：可按商品、等级筛选 */
    @GetMapping("/calc/page")
    public R<PageResult<PriceCalc>> calcPage(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String grade,
                                            @RequestParam(required = false) Long goods_id) {
        QueryWrapper<PriceCalc> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        if (grade != null && !grade.isBlank()) {
            w.eq("grade", grade);
        }
        if (goods_id != null) {
            w.eq("goods_id", goods_id);
        }
        w.orderByDesc("create_time");
        IPage<PriceCalc> p = calcMapper.selectPage(new Page<>(page, size), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    @GetMapping("/calc/{id}")
    public R<PriceCalc> calc(@PathVariable Long id) {
        return R.ok(calcMapper.selectById(id));
    }

    /** 重新计算并留痕（换规则后主动重算） */
    @PostMapping("/calc")
    public R<Map<String, Object>> recalc(@RequestParam Long goodsId) {
        return R.ok(priceService.calculate(goodsId, true));
    }

    /** 人工修正：保留原始结果，只记录修正等级、原因与操作人 */
    @PostMapping("/calc/{id}/correct")
    public R<Void> correct(@PathVariable Long id, @RequestBody @jakarta.validation.Valid Dtos.PriceCorrectReq req) {
        priceService.correct(id, req);
        return R.ok();
    }

    /** 卖家采纳建议价 */
    @PostMapping("/calc/{id}/adopt")
    public R<Void> adopt(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean adopted) {
        priceService.adopt(id, adopted);
        return R.ok();
    }

    /** 采纳率与修正率统计 */
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(priceService.stats());
    }
}
