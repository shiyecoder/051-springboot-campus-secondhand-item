package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 装车记录 */
@Data
@TableName("lg_load")
public class LgLoad {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 装车单号 */
    private String loadNo;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 计划重量 */
    private BigDecimal planWeight;
    /** 计划体积 */
    private BigDecimal planVolume;
    /** 实际重量 */
    private BigDecimal realWeight;
    /** 实际体积 */
    private BigDecimal realVolume;
    /** 差异率 % */
    private BigDecimal diffRate;
    /** 差异判定 NORMAL/OVER */
    private String diffFlag;
    /** 装车时间 */
    private String loadTime;
    /** 现场联系人 */
    private String siteContact;
    /** 货物照片（多张，逗号分隔） */
    private String cargoImages;
    /** 状态 PENDING/CONFIRMED/REJECTED */
    private String status;
    /** 确认人 */
    private String confirmBy;
    /** 确认时间 */
    private String confirmTime;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private String createTime;
}
