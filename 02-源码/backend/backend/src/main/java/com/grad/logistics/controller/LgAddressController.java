package com.grad.logistics.controller;

import com.grad.logistics.entity.LgAddress;
import com.grad.logistics.mapper.LgAddressMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 客户常用地址 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/address")
public class LgAddressController extends BaseCrudController<LgAddress> {

    private final LgAddressMapper mapper;

    public LgAddressController(LgAddressMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgAddress> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgAddress> type() {
        return LgAddress.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("customer_id", "addr_type", "city", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("address", "contact", "customer_name");
    }
}
