package com.grad.logistics.controller;

import com.grad.logistics.entity.LgDriverCert;
import com.grad.logistics.mapper.LgDriverCertMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 驾驶员证件 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/dcert")
public class LgDriverCertController extends BaseCrudController<LgDriverCert> {

    private final LgDriverCertMapper mapper;

    public LgDriverCertController(LgDriverCertMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgDriverCert> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgDriverCert> type() {
        return LgDriverCert.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("driver_id", "cert_type", "status", "expire_date");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("driver_name", "cert_no");
    }
}
