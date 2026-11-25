package com.grad.logistics.controller;

import com.grad.logistics.entity.LgVehicle;
import com.grad.logistics.mapper.LgVehicleMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 车辆档案 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/vehicle")
public class LgVehicleController extends BaseCrudController<LgVehicle> {

    private final LgVehicleMapper mapper;

    public LgVehicleController(LgVehicleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgVehicle> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgVehicle> type() {
        return LgVehicle.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("status", "vehicle_type", "fleet_name", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("plate_no", "fleet_name");
    }
}
