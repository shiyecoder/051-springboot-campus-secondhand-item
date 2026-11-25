package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 数据字典 */
@Data
@TableName("sys_dict")
public class SysDict {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 字典类型 */
    private String dictType;
    /** 标签 */
    private String dictLabel;
    /** 值 */
    private String dictValue;
    /** 排序 */
    private Integer sort;
    /** 状态 */
    private Integer status;
    /** 创建时间 */
    private String createTime;
}
