package com.grad.logistics.controller;

import com.grad.logistics.entity.LgComm;
import com.grad.logistics.mapper.LgCommMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 异常沟通记录 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/comm")
public class LgCommController extends BaseCrudController<LgComm> {

    private final LgCommMapper mapper;

    public LgCommController(LgCommMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgComm> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgComm> type() {
        return LgComm.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("exception_id", "exception_no");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("content", "operator");
    }
}
