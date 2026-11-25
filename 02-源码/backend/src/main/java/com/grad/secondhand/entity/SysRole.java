package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 角色（表 sys_role） */
@Data
@TableName("sys_role")
public class SysRole {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 角色名称 */
    private String roleName;
    /** 角色编码 */
    private String roleCode;
    /** all/dept/self */
    private String dataScope;
    /** 1启用 0停用 */
    private Integer status;
    /** 备注 */
    private String remark;
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
