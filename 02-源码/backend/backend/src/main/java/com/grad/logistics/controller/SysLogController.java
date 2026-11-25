package com.grad.logistics.controller;

import com.grad.logistics.entity.SysLog;
import com.grad.logistics.mapper.SysLogMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 操作日志 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/log")
public class SysLogController extends BaseCrudController<SysLog> {

    private final SysLogMapper mapper;

    public SysLogController(SysLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<SysLog> mapper() {
        return mapper;
    }

    @Override
    protected Class<SysLog> type() {
        return SysLog.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("username", "action", "create_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("username", "action", "detail");
    }
}
