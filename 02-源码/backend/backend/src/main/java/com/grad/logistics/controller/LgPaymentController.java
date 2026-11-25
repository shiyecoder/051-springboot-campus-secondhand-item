package com.grad.logistics.controller;

import com.grad.logistics.entity.LgPayment;
import com.grad.logistics.mapper.LgPaymentMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 线下收付款登记 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/payment")
public class LgPaymentController extends BaseCrudController<LgPayment> {

    private final LgPaymentMapper mapper;

    public LgPaymentController(LgPaymentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgPayment> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgPayment> type() {
        return LgPayment.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("direction", "status", "pay_method", "pay_date");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("payment_no", "target_name", "ref_no");
    }
}
