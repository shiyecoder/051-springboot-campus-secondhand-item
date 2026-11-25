package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 系统用户 */
@Data
@TableName("sys_user")
public class SysUser {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 登录名 */
    private String username;
    /** BCrypt 密码 */
    private String password;
    /** 姓名 */
    private String realName;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 组织ID */
    private Long orgId;
    /** 组织名称（冗余） */
    private String orgName;
    /** 头像 */
    private String avatar;
    /** 性别 */
    private String gender;
    /** 状态 1启用 0停用 */
    private Integer status;
    /** 连续失败次数 */
    private Integer loginFailCount;
    /** 锁定至 */
    private String lockUntil;
    /** 最近登录 */
    private String lastLogin;
    /** 逻辑删除 */
    private Integer deleted;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
}
