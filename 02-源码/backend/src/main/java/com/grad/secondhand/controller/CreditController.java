package com.grad.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.PageResult;
import com.grad.secondhand.common.R;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.CreditLog;
import com.grad.secondhand.mapper.CreditLogMapper;
import com.grad.secondhand.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** 信用分：流水查询与人工调整（需求 5.4，上下限 0–150） */
@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditLogMapper creditMapper;
    private final TradeService tradeService;

    /** 流水分页：学生只看本人，运营与管理员可按用户查 */
    @GetMapping("/page")
    public R<PageResult<CreditLog>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) Long user_id,
                                         @RequestParam(required = false) String reason_type) {
        QueryWrapper<CreditLog> w = new QueryWrapper<>();
        w.eq("deleted", 0);
        if (UserContext.isStudent()) {
            w.eq("user_id", UserContext.userId());
        } else if (user_id != null) {
            w.eq("user_id", user_id);
        }
        if (reason_type != null && !reason_type.isBlank()) {
            w.eq("reason_type", reason_type);
        }
        w.orderByDesc("create_time");
        IPage<CreditLog> p = creditMapper.selectPage(new Page<>(page, size), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /** 人工调整：原因不少于 5 字，写流水并同步用户分值 */
    @PostMapping("/adjust")
    public R<Map<String, Object>> adjust(@RequestBody @jakarta.validation.Valid Dtos.CreditAdjustReq req) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有校园运营人员可以调整信用分");
        }
        tradeService.adjustCredit(req.userId(), req.changeValue(), "人工调整", null, req.remark());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", req.userId());
        data.put("changeValue", req.changeValue());
        return R.ok(data);
    }
}
