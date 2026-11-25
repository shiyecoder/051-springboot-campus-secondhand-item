package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 操作日志（表 sys_oper_log） */
@Data
@TableName("sys_oper_log")
public class SysOperLog {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 模块 */
    private String module;
    /** 操作类型 */
    private String operType;
    /** 业务编号 */
    private String bizNo;
    /** 操作内容 */
    private String content;
    /** 操作人ID */
    private Long operId;
    /** 操作人 */
    private String operName;
    /** 操作时间 */
    private String operTime;
    /** IP */
    private String ip;
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
