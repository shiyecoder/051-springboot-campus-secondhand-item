package com.grad.logistics.controller;

import com.grad.logistics.entity.LgDemand;
import com.grad.logistics.mapper.LgDemandMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 运输需求 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/demand")
public class LgDemandController extends BaseCrudController<LgDemand> {

    private final LgDemandMapper mapper;

    public LgDemandController(LgDemandMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgDemand> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgDemand> type() {
        return LgDemand.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("customer_id", "status", "goods_type", "expect_load_time", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("demand_no", "customer_name", "goods_name");
    }
}
