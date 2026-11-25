package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 异常沟通记录 */
@Data
@TableName("lg_comm")
public class LgComm {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 异常ID */
    private Long exceptionId;
    /** 异常编号（冗余） */
    private String exceptionNo;
    /** 沟通方式 电话/微信/现场/邮件 */
    private String way;
    /** 沟通内容 */
    private String content;
    /** 沟通人 */
    private String operator;
    /** 沟通时间 */
    private String commTime;
    /** 创建时间 */
    private String createTime;
}
