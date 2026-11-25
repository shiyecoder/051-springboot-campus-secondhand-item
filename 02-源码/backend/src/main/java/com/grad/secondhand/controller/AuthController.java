package com.grad.secondhand.controller;

import com.grad.secondhand.common.R;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.service.AuthService;
import com.grad.secondhand.service.DashboardService;
import com.grad.secondhand.service.TraceService;
import com.grad.secondhand.common.UserContext;
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
    private final TraceService trace;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody Dtos.LoginReq req, HttpServletRequest request) {
        return R.ok(authService.login(req, request.getRemoteAddr()));
    }

    /** 登录页展示的平台规模（公开接口），避免页面写死与库里不一致的假数字 */
    @GetMapping("/site-stats")
    public R<Map<String, Object>> siteStats() {
        return R.ok(dashboardService.siteStats());
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(authService.me());
    }

    /** 顶栏未读消息数 */
    @GetMapping("/unread")
    public R<Long> unread() {
        Long uid = UserContext.userId();
        return R.ok(uid == null ? 0L : trace.unread(uid));
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
