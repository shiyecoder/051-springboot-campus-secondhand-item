package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 组织与车队 */
@Data
@TableName("sys_org")
public class SysOrg {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 上级 */
    private Long parentId;
    /** 组织名称 */
    private String orgName;
    /** 类型 COMPANY/FLEET/DEPT */
    private String orgType;
    /** 负责人 */
    private String leader;
    /** 排序 */
    private Integer sort;
    /** 状态 */
    private Integer status;
    /** 创建时间 */
    private String createTime;
}
