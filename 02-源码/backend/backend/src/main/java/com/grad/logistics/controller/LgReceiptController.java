package com.grad.logistics.controller;

import com.grad.logistics.entity.LgReceipt;
import com.grad.logistics.mapper.LgReceiptMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 回单与签收凭证 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/receipt")
public class LgReceiptController extends BaseCrudController<LgReceipt> {

    private final LgReceiptMapper mapper;

    public LgReceiptController(LgReceiptMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgReceipt> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgReceipt> type() {
        return LgReceipt.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("status", "paper_status", "order_no", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("receipt_no", "order_no", "uploader");
    }
}
