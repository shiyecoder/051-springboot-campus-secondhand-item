package com.grad.secondhand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园二手物品交易平台启动类（NEW-051）。
 * Mapper 统一扫描 com.grad.secondhand.mapper；开启定时任务用于订单超时自动取消（每 5 分钟）。
 */
@SpringBootApplication
@MapperScan("com.grad.secondhand.mapper")
@EnableScheduling
public class SecondHandApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecondHandApplication.class, args);
    }
}
