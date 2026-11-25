package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 客户档案 */
@Data
@TableName("lg_customer")
public class LgCustomer {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 客户编号 */
    private String customerNo;
    /** 客户名称 */
    private String name;
    /** 联系人 */
    private String contact;
    /** 联系电话 */
    private String phone;
    /** 结算周期 */
    private String settlementCycle;
    /** 信用状态 NORMAL/WARN/DISABLED */
    private String creditStatus;
    /** 开票抬头 */
    private String invoiceTitle;
    /** 税号 */
    private String taxNo;
    /** 开户行与账号 */
    private String bankAccount;
    /** 累计运单数 */
    private Integer totalOrders;
    /** 累计费用 */
    private BigDecimal totalAmount;
    /** 备注 */
    private String remark;
    /** 逻辑删除 */
    private Integer deleted;
    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
    /** 乐观锁版本号 */
    private Integer version;
}
