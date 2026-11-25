package com.grad.logistics.controller;

import com.grad.logistics.entity.LgException;
import com.grad.logistics.mapper.LgExceptionMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 异常事件 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/exception")
public class LgExceptionController extends BaseCrudController<LgException> {

    private final LgExceptionMapper mapper;

    public LgExceptionController(LgExceptionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgException> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgException> type() {
        return LgException.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("status", "type", "level", "order_no", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("exception_no", "order_no", "description");
    }
}
