package com.grad.secondhand.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grad.secondhand.common.R;
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
 * 安全配置：无状态 JWT，放行登录、注册与上传目录。
 * 系统管理与规则配置按角色收紧，避免「前端隐藏按钮但直接调接口」的越权；
 * 业务写接口（审核、举报处理、订单流转）在 Service 内二次校验角色与数据范围。
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
                .requestMatchers("/api/auth/login", "/api/auth/site-stats", "/uploads/**", "/error").permitAll()
                // 门户公开浏览：未登录也能看首页与商品详情
                // 门户游客可见：首页统计、分类树、商品列表与详情、面交点
                .requestMatchers(HttpMethod.GET, "/api/dashboard/portal", "/api/category/tree",
                        "/api/goods/search", "/api/goods/*", "/api/goods/*/price-reference",
                        "/api/category/page", "/api/campus-place/list").permitAll()
                // 系统管理：仅平台管理员
                .requestMatchers("/api/system/**").hasRole("ADMIN")
                // 定价规则保存：运营与管理员
                .requestMatchers(HttpMethod.POST, "/api/price/rules").hasAnyRole("OPERATOR", "ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> write(res, 4010, "登录状态已失效，请重新登录"))
                .accessDeniedHandler((req, res, ex) -> write(res, 4030, "没有该操作的权限")))
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
