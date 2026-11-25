package com.grad.logistics.controller;

import com.grad.logistics.entity.LgReview;
import com.grad.logistics.mapper.LgReviewMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 服务评价 通用 CRUD 接口 */
@RestController
@RequestMapping("/api/review")
public class LgReviewController extends BaseCrudController<LgReview> {

    private final LgReviewMapper mapper;

    public LgReviewController(LgReviewMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<LgReview> mapper() {
        return mapper;
    }

    @Override
    protected Class<LgReview> type() {
        return LgReview.class;
    }

    @Override
    protected List<String> filterColumns() {
        return List.of("order_id", "order_no", "score", "status");
    }

    @Override
    protected List<String> likeColumns() {
        return List.of("review_no", "customer_name", "content");
    }
}
