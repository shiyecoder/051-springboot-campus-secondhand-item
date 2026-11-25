package com.grad.logistics.controller;

import com.grad.logistics.entity.LgScoreItem;
import com.grad.logistics.mapper.LgScoreItemMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 配车评分明细 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/score-item")
public class LgScoreItemController extends BaseCrudController<LgScoreItem> {

    private final LgScoreItemMapper mapper;

    public LgScoreItemController(LgScoreItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgScoreItem> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgScoreItem> type() {
        return LgScoreItem.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("score_id");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("dimension");
    }
}
