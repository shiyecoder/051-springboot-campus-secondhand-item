package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 角色菜单授权 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 角色ID */
    private Long roleId;
    /** 菜单ID */
    private Long menuId;
}
