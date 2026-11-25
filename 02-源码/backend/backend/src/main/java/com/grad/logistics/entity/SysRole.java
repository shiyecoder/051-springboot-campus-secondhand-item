package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 角色 */
@Data
@TableName("sys_role")
public class SysRole {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 角色编码 */
    private String roleCode;
    /** 角色名称 */
    private String roleName;
    /** 数据范围 all/dept/self/org */
    private String dataScope;
    /** 说明 */
    private String remark;
    /** 状态 */
    private Integer status;
    /** 创建时间 */
    private String createTime;
}
