package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 需求货物明细 */
@Data
@TableName("lg_demand_item")
public class LgDemandItem {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 需求ID */
    private Long demandId;
    /** 品名 */
    private String goodsName;
    /** 规格 */
    private String spec;
    /** 数量 */
    private Integer quantity;
    /** 单位 */
    private String unit;
    /** 重量（吨） */
    private BigDecimal weight;
    /** 体积（m³） */
    private BigDecimal volume;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private String createTime;
}
