package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 签收记录 */
@Data
@TableName("lg_sign")
public class LgSign {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 应收数量 */
    private Integer planQty;
    /** 实收数量 */
    private Integer realQty;
    /** 差异说明 */
    private String diffNote;
    /** 签收人 */
    private String signer;
    /** 签收人电话 */
    private String signPhone;
    /** 签收时间 */
    private String signTime;
    /** 签收凭证 */
    private String images;
    /** 状态 SIGNED/DISPUTE */
    private String status;
    /** 创建时间 */
    private String createTime;
}
