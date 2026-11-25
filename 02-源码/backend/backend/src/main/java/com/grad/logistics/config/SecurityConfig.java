package com.grad.logistics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grad.logistics.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 安全配置：无状态 JWT，放行登录与上传目录。
 * 系统管理、规则配置、经理审批类接口按角色收紧，避免「前端隐藏按钮但直接调接口」的越权。
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**", "/uploads/**", "/error").permitAll()
                // 系统管理：用户、角色、菜单、字典、日志
                .requestMatchers("/api/system/**").hasRole("MANAGER")
                // 规则配置：仅经理可改权重与阈值
                .requestMatchers(HttpMethod.POST, "/api/rule/**").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/rule/**").hasRole("MANAGER")
                // 调度配车：调度员与经理
                .requestMatchers(HttpMethod.POST, "/api/dispatch/**").hasAnyRole("MANAGER", "DISPATCHER")
                // 运输节点：驾驶员上报，财务不可修改
                .requestMatchers(HttpMethod.POST, "/api/node/**").hasAnyRole("DRIVER", "DISPATCHER", "MANAGER")
                // 费用：驾驶员提交、财务审核
                .requestMatchers(HttpMethod.POST, "/api/fee/**").hasAnyRole("DRIVER", "FINANCE", "MANAGER")
                // 客户档案与地址：客户可提需求，档案维护由调度与经理
                .requestMatchers(HttpMethod.POST, "/api/customer/**", "/api/address/**")
                    .hasAnyRole("MANAGER", "DISPATCHER")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> write(res, 401, "登录状态已失效，请重新登录"))
                .accessDeniedHandler((req, res, ex) -> write(res, 403, "没有该操作的权限")))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(f -> f.disable())
            .httpBasic(b -> b.disable())
            .logout(l -> l.disable());
        return http.build();
    }

    private void write(jakarta.servlet.http.HttpServletResponse res, int code, String msg) throws java.io.IOException {
        res.setStatus(200);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(objectMapper.writeValueAsString(R.fail(code, msg)));
    }

    private CorsConfigurationSource corsSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}
