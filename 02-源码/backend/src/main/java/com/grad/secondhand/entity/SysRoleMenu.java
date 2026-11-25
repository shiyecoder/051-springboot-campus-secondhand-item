package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 角色菜单（表 sys_role_menu） */
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
