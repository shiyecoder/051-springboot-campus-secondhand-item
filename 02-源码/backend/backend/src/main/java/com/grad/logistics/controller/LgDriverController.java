package com.grad.logistics.controller;

import com.grad.logistics.entity.LgDriver;
import com.grad.logistics.mapper.LgDriverMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 驾驶员档案 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/driver")
public class LgDriverController extends BaseCrudController<LgDriver> {

    private final LgDriverMapper mapper;

    public LgDriverController(LgDriverMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgDriver> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgDriver> type() {
        return LgDriver.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("status", "fleet_name", "license_type", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("name", "driver_no", "phone");
    }
}
