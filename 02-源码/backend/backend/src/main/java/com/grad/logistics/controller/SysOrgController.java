package com.grad.logistics.controller;

import com.grad.logistics.entity.SysOrg;
import com.grad.logistics.mapper.SysOrgMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 组织与车队 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/org")
public class SysOrgController extends BaseCrudController<SysOrg> {

    private final SysOrgMapper mapper;

    public SysOrgController(SysOrgMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<SysOrg> mapper() {
        return mapper;
    }

    @Override
    protected Class<SysOrg> type() {
        return SysOrg.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("parent_id", "status", "org_type");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("org_name", "leader");
    }
}
