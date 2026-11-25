package com.grad.logistics.dto;

import jakarta.validation.constraints.NotBlank;

/** 请求 DTO 集合。字段对外为 snake_case（全局 Jackson 命名策略）。 */
public class Dtos {

    public record LoginReq(@NotBlank(message = "请输入用户名") String username,
                           @NotBlank(message = "请输入密码") String password) {
    }

    public record ProfileReq(String realName, String phone, String avatar) {
    }

    public record PasswordReq(@NotBlank(message = "请输入原密码") String oldPassword,
                              @NotBlank(message = "请输入新密码") String newPassword) {
    }

    /** 通用状态流转：目标状态 + 原因（退回、驳回、取消、改派必须填写，且不少于 5 个字） */
    public record TransitionReq(String target, String reason, String place, String description) {
    }

    /** 人工修正配车结果：必须填写原因，原始计算结果不覆盖 */
    public record ManualReq(String plateNo, String vehicleId, String reason) {
    }

    /** 配车请求 */
    public record AssignReq(Long orderId, Long vehicleId, Long driverId, String planLoadTime,
                            String planArriveTime, String reason) {
    }

    /** 费用审核 / 回单复核 */
    public record AuditReq(String action, String note) {
    }
}
