package com.grad.logistics.controller;

import com.grad.logistics.entity.LgFeeType;
import com.grad.logistics.mapper.LgFeeTypeMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 费用类型 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/fee-type")
public class LgFeeTypeController extends BaseCrudController<LgFeeType> {

    private final LgFeeTypeMapper mapper;

    public LgFeeTypeController(LgFeeTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgFeeType> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgFeeType> type() {
        return LgFeeType.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("direction", "status");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("type_name", "type_code");
    }
}
