package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 费用记录 */
@Data
@TableName("lg_fee")
public class LgFee {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 费用单号 */
    private String feeNo;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 费用类型 */
    private String feeType;
    /** 方向 RECV/PAY */
    private String direction;
    /** 金额（负数表示赔付） */
    private BigDecimal amount;
    /** 凭证图片 */
    private String voucher;
    /** 状态 PENDING/CHECKED/REJECTED */
    private String status;
    /** 提交人 */
    private String submitter;
    /** 提交时间 */
    private String submitTime;
    /** 审核人 */
    private String auditBy;
    /** 审核时间 */
    private String auditTime;
    /** 审核意见 */
    private String auditNote;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
}
