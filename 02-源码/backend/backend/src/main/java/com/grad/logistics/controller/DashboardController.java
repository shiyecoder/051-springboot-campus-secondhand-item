package com.grad.logistics.controller;

import com.grad.logistics.common.R;
import com.grad.logistics.common.UserContext;
import com.grad.logistics.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 运营看板、统计报表与我的待办 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(dashboardService.overview());
    }

    @GetMapping("/analysis")
    public R<Map<String, Object>> analysis() {
        return R.ok(dashboardService.analysis());
    }

    @GetMapping("/todo")
    public R<Map<String, Object>> todo() {
        return R.ok(dashboardService.todo(UserContext.roleCode(), UserContext.realName()));
    }
}
