package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 客户账单 */
@Data
@TableName("lg_bill")
public class LgBill {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 账单编号 */
    private String billNo;
    /** 客户ID */
    private Long customerId;
    /** 客户名称（冗余） */
    private String customerName;
    /** 账期 yyyy-MM */
    private String period;
    /** 运单数 */
    private Integer orderCount;
    /** 账单金额 */
    private BigDecimal totalAmount;
    /** 已收金额 */
    private BigDecimal paidAmount;
    /** 未收金额 */
    private BigDecimal unpaidAmount;
    /** 状态 UNPAID/PART/PAID/CHECKED */
    private String status;
    /** 出账日期 */
    private String billDate;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private String createTime;
}
