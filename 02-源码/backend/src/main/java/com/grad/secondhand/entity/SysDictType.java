package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 字典类型（表 sys_dict_type） */
@Data
@TableName("sys_dict_type")
public class SysDictType {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 字典名称 */
    private String dictName;
    /** 字典类型编码 */
    private String dictType;
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
