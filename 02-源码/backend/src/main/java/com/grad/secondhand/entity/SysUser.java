package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 用户（表 sys_user） */
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
    /** 姓名/昵称 */
    private String realName;
    /** 学号，学生账号导入时绑定 */
    private String studentNo;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 头像相对路径 */
    private String avatar;
    /** 男/女 */
    private String gender;
    /** 所属部门（运营人员） */
    private Long deptId;
    /** 绑定校区编号 */
    private Integer campusId;
    /** 绑定校区名称 */
    private String campusName;
    /** 信用分 0-150 */
    private Integer creditScore;
    /** 1启用 0停用 */
    private Integer status;
    /** 连续登录失败次数 */
    private Integer loginFailCount;
    /** 锁定至 */
    private String lockUntil;
    /** 最近登录时间 */
    private String lastLogin;
    /** 创建人 */
    private Long createBy;
    /** 创建时间 */
    private String createTime;
    /** 更新人 */
    private Long updateBy;
    /** 更新时间 */
    private String updateTime;
    /** 0正常 1删除 */
    private Integer deleted;
    /** 乐观锁版本号 */
    private Integer version;
}
