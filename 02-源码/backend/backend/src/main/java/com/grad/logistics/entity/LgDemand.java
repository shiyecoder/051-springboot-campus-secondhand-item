package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 运输需求 */
@Data
@TableName("lg_demand")
public class LgDemand {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 需求编号 */
    private String demandNo;
    /** 客户ID */
    private Long customerId;
    /** 客户名称（冗余） */
    private String customerName;
    /** 货物类型（字典） */
    private String goodsType;
    /** 货物名称 */
    private String goodsName;
    /** 件数 */
    private Integer quantity;
    /** 重量（吨） */
    private BigDecimal weight;
    /** 体积（m³） */
    private BigDecimal volume;
    /** 发货地址 */
    private String fromAddress;
    /** 收货地址 */
    private String toAddress;
    /** 期望装货时间 */
    private String expectLoadTime;
    /** 要求送达时间 */
    private String expectArriveTime;
    /** 状态 SUBMITTED/AUDITED/ORDERED/CANCELED */
    private String status;
    /** 审核人 */
    private String auditBy;
    /** 审核时间 */
    private String auditTime;
    /** 审核意见 */
    private String auditRemark;
    /** 联系人 */
    private String contact;
    /** 联系电话 */
    private String contactPhone;
    /** 货物图片 */
    private String image;
    /** 特殊要求 */
    private String remark;
    /** 逻辑删除 */
    private Integer deleted;
    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
}
