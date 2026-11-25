package com.grad.logistics.controller;

import com.grad.logistics.entity.LgRule;
import com.grad.logistics.mapper.LgRuleMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 配车规则版本 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/rule")
public class LgRuleController extends BaseCrudController<LgRule> {

    private final LgRuleMapper mapper;

    public LgRuleController(LgRuleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgRule> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgRule> type() {
        return LgRule.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("status");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("version", "remark");
    }
}
