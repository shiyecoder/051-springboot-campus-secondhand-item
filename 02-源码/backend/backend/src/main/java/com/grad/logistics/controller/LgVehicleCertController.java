package com.grad.logistics.controller;

import com.grad.logistics.entity.LgVehicleCert;
import com.grad.logistics.mapper.LgVehicleCertMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 车辆证件 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/vcert")
public class LgVehicleCertController extends BaseCrudController<LgVehicleCert> {

    private final LgVehicleCertMapper mapper;

    public LgVehicleCertController(LgVehicleCertMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgVehicleCert> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgVehicleCert> type() {
        return LgVehicleCert.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("vehicle_id", "plate_no", "cert_type", "status", "expire_date");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("plate_no", "cert_no");
    }
}
