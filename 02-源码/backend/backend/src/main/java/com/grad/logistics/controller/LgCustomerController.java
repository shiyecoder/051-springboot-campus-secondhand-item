package com.grad.logistics.controller;

import com.grad.logistics.entity.LgCustomer;
import com.grad.logistics.mapper.LgCustomerMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 客户档案 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/customer")
public class LgCustomerController extends BaseCrudController<LgCustomer> {

    private final LgCustomerMapper mapper;

    public LgCustomerController(LgCustomerMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgCustomer> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgCustomer> type() {
        return LgCustomer.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("settlement_cycle", "credit_status", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("name", "contact", "customer_no");
    }
}
