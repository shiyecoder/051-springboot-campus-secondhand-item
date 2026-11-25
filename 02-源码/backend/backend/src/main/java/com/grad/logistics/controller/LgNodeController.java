package com.grad.logistics.controller;

import com.grad.logistics.entity.LgNode;
import com.grad.logistics.mapper.LgNodeMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 运输节点 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/node")
public class LgNodeController extends BaseCrudController<LgNode> {

    private final LgNodeMapper mapper;

    public LgNodeController(LgNodeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgNode> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgNode> type() {
        return LgNode.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("order_id", "order_no", "node_type", "node_time");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("order_no", "place", "description");
    }
}
