package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 菜单权限（表 sys_menu） */
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
    /** 路由地址 */
    private String path;
    /** 组件路径 */
    private String component;
    /** 权限标识 */
    private String perm;
    /** catalog/menu/button */
    private String menuType;
    /** 图标键 */
    private String icon;
    /** 排序号 */
    private Integer sort;
    /** 1显示 0隐藏 */
    private Integer visible;
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
