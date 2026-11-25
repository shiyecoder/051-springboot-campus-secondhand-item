package com.grad.logistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 物流车辆调度与运单跟踪管理系统启动类。
 * Mapper 统一扫描 com.grad.logistics.mapper，避免每个接口重复加 @Mapper。
 */
@SpringBootApplication
@MapperScan("com.grad.logistics.mapper")
public class LogisticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogisticsApplication.class, args);
    }
}
