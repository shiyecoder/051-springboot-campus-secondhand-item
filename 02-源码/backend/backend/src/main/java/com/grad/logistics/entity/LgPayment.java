package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 线下收付款登记 */
@Data
@TableName("lg_payment")
public class LgPayment {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 流水号 */
    private String paymentNo;
    /** 账单ID */
    private Long billId;
    /** 关联单号 */
    private String refNo;
    /** 往来单位 */
    private String targetName;
    /** 方向 RECV/PAY */
    private String direction;
    /** 金额 */
    private BigDecimal amount;
    /** 方式 银行转账/承兑汇票/现金/对公转账 */
    private String payMethod;
    /** 状态 REGISTERED/PENDING/CHECKED/PAID */
    private String status;
    /** 日期 */
    private String payDate;
    /** 登记人 */
    private String operator;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private String createTime;
}
