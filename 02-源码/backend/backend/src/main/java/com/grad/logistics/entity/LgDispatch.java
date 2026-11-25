package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 车辆调度任务 */
@Data
@TableName("lg_dispatch")
public class LgDispatch {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 调度单号 */
    private String dispatchNo;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 客户（冗余） */
    private String customerName;
    /** 始发城市 */
    private String fromCity;
    /** 目的城市 */
    private String toCity;
    /** 货物与载重描述 */
    private String goodsDesc;
    /** 车辆ID */
    private Long vehicleId;
    /** 车牌号 */
    private String plateNo;
    /** 驾驶员ID */
    private Long driverId;
    /** 驾驶员 */
    private String driverName;
    /** 计划装货时间 */
    private String planLoadTime;
    /** 预计到达时间 */
    private String planArriveTime;
    /** 状态 TODO/PROCESSING/DONE/EXCEPTION/CANCELED */
    private String status;
    /** 原车辆（改派留痕） */
    private String originVehicle;
    /** 原驾驶员（改派留痕） */
    private String originDriver;
    /** 变更原因 */
    private String changeReason;
    /** 变更时间 */
    private String changeTime;
    /** 调度员 */
    private String dispatcher;
    /** 配车评分ID */
    private Long scoreId;
    /** 处理时限 */
    private String deadline;
    /** 进度说明 */
    private String remark;
    /** 逻辑删除 */
    private Integer deleted;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
}
