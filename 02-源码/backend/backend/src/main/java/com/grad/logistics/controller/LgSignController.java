package com.grad.logistics.controller;

import com.grad.logistics.entity.LgSign;
import com.grad.logistics.mapper.LgSignMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 签收记录 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/sign")
public class LgSignController extends BaseCrudController<LgSign> {

    private final LgSignMapper mapper;

    public LgSignController(LgSignMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgSign> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgSign> type() {
        return LgSign.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("order_id", "order_no", "status");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("order_no", "signer");
    }
}
