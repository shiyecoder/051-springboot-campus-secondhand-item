package com.grad.logistics.controller;

import com.grad.logistics.entity.LgScore;
import com.grad.logistics.mapper.LgScoreMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 配车评分与计算快照 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/score")
public class LgScoreController extends BaseCrudController<LgScore> {

    private final LgScoreMapper mapper;

    public LgScoreController(LgScoreMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgScore> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgScore> type() {
        return LgScore.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("order_id", "order_no", "result_level", "manual_flag");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("order_no", "plate_no");
    }
}
