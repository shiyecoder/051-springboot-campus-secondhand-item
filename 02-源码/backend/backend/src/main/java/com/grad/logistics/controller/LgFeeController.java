package com.grad.logistics.controller;

import com.grad.logistics.entity.LgFee;
import com.grad.logistics.mapper.LgFeeMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 费用记录 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/fee")
public class LgFeeController extends BaseCrudController<LgFee> {

    private final LgFeeMapper mapper;

    public LgFeeController(LgFeeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgFee> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgFee> type() {
        return LgFee.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("order_id", "order_no", "fee_type", "direction", "status", "submit_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("fee_no", "order_no", "submitter");
    }
}
