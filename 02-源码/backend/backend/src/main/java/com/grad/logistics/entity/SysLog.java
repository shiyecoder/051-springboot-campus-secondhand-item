package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 操作日志 */
@Data
@TableName("sys_log")
public class SysLog {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 操作人 */
    private String username;
    /** 动作 */
    private String action;
    /** 对象 */
    private String target;
    /** 详情 */
    private String detail;
    /** IP */
    private String ip;
    /** 创建时间 */
    private String createTime;
}
