package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 菜单 */
@Data
@TableName("sys_menu")
public class SysMenu {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 上级菜单 */
    private Long parentId;
    /** 菜单名称 */
    private String menuName;
    /** 类型 catalog/menu/button */
    private String menuType;
    /** 路由路径 */
    private String path;
    /** 权限标识 */
    private String perm;
    /** 图标 */
    private String icon;
    /** 排序 */
    private Integer sort;
    /** 是否可见 */
    private Integer visible;
    /** 创建时间 */
    private String createTime;
}
