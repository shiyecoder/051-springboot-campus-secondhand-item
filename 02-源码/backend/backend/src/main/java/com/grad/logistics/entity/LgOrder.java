package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 运单主档 */
@Data
@TableName("lg_order")
public class LgOrder {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 运单编号 */
    private String orderNo;
    /** 来源需求ID */
    private Long demandId;
    /** 客户ID */
    private Long customerId;
    /** 发货方（冗余） */
    private String customerName;
    /** 收货方 */
    private String receiverName;
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
    /** 始发城市 */
    private String fromCity;
    /** 目的城市 */
    private String toCity;
    /** 装货地址 */
    private String fromAddress;
    /** 卸货地址 */
    private String toAddress;
    /** 要求车型 */
    private String vehicleTypeReq;
    /** 计划里程 km */
    private BigDecimal planDistance;
    /** 计划时效（小时） */
    private BigDecimal planHours;
    /** 计划装货时间 */
    private String planLoadTime;
    /** 预计到达时间 */
    private String planArriveTime;
    /** 要求送达时间 */
    private String requireArriveTime;
    /** 实际到达时间 */
    private String actualArriveTime;
    /** 车辆ID */
    private Long vehicleId;
    /** 车牌号（冗余） */
    private String plateNo;
    /** 驾驶员ID */
    private Long driverId;
    /** 驾驶员姓名（冗余） */
    private String driverName;
    /** 状态 CREATED/DISPATCHED/LOADED/SHIPPING/ARRIVED/SIGNED/FEE_CHECK/DONE/EXCEPTION/CANCELED */
    private String status;
    /** 异常标记 NORMAL/DELAY/DAMAGE/REJECT/OTHER */
    private String exceptionFlag;
    /** 费用合计 */
    private BigDecimal feeTotal;
    /** 应收合计 */
    private BigDecimal receivable;
    /** 货物图片 */
    private String cargoImage;
    /** 特殊注意事项 */
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
