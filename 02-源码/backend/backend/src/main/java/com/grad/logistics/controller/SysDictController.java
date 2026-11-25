package com.grad.logistics.controller;

import com.grad.logistics.entity.SysDict;
import com.grad.logistics.mapper.SysDictMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 数据字典 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/dict")
public class SysDictController extends BaseCrudController<SysDict> {

    private final SysDictMapper mapper;

    public SysDictController(SysDictMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<SysDict> mapper() {
        return mapper;
    }

    @Override
    protected Class<SysDict> type() {
        return SysDict.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("dict_type", "status");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("dict_label", "dict_value");
    }
}
