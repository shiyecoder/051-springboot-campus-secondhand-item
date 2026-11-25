package com.grad.logistics.controller;

import com.grad.logistics.entity.LgLoad;
import com.grad.logistics.mapper.LgLoadMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 装车记录 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/load")
public class LgLoadController extends BaseCrudController<LgLoad> {

    private final LgLoadMapper mapper;

    public LgLoadController(LgLoadMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgLoad> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgLoad> type() {
        return LgLoad.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("status", "diff_flag", "order_no", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("load_no", "order_no");
    }
}
