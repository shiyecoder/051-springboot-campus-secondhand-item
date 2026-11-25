package com.grad.logistics.controller;

import com.grad.logistics.common.R;
import com.grad.logistics.dto.Dtos;
import com.grad.logistics.service.AuthService;
import com.grad.logistics.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 认证与个人中心：登录返回 token、用户信息与菜单树 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DashboardService dashboardService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody Dtos.LoginReq req, HttpServletRequest request) {
        return R.ok(authService.login(req, request.getRemoteAddr()));
    }

    /** 登录页经营数据（公开），避免页面上写死与库里不一致的假数字 */
    @GetMapping("/site-stats")
    public R<Map<String, Object>> siteStats() {
        return R.ok(dashboardService.siteStats());
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(authService.me());
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@Valid @RequestBody Dtos.ProfileReq req) {
        authService.updateProfile(req);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> updatePassword(@Valid @RequestBody Dtos.PasswordReq req) {
        authService.updatePassword(req);
        return R.ok();
    }
}
