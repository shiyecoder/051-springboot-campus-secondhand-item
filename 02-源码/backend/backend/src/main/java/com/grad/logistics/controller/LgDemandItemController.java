package com.grad.logistics.controller;

import com.grad.logistics.entity.LgDemandItem;
import com.grad.logistics.mapper.LgDemandItemMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 需求货物明细 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/demand-item")
public class LgDemandItemController extends BaseCrudController<LgDemandItem> {

    private final LgDemandItemMapper mapper;

    public LgDemandItemController(LgDemandItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgDemandItem> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgDemandItem> type() {
        return LgDemandItem.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("demand_id");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("goods_name");
    }
}
