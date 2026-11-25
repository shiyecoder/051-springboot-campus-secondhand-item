package com.grad.logistics.controller;

import com.grad.logistics.entity.LgBill;
import com.grad.logistics.mapper.LgBillMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 客户账单 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/bill")
public class LgBillController extends BaseCrudController<LgBill> {

    private final LgBillMapper mapper;

    public LgBillController(LgBillMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgBill> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgBill> type() {
        return LgBill.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("customer_id", "period", "status", "bill_date");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("bill_no", "customer_name");
    }
}
