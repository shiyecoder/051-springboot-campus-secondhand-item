package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 车辆档案 */
@Data
@TableName("lg_vehicle")
public class LgVehicle {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 车牌号 */
    private String plateNo;
    /** 车型 9.6m厢式/13m平板/7.6m冷藏/9.6m高栏/13m厢式 */
    private String vehicleType;
    /** 核载（吨） */
    private BigDecimal loadWeight;
    /** 容积（m³） */
    private BigDecimal volume;
    /** 能源类型 */
    private String energyType;
    /** 所属车队 */
    private String fleetName;
    /** 车主/挂靠人 */
    private String ownerName;
    /** 状态 AVAILABLE/RUNNING/MAINT/STOP */
    private String status;
    /** 车辆照片（本地相对路径） */
    private String image;
    /** 累计任务数 */
    private Integer totalTrips;
    /** 累计里程 km */
    private BigDecimal mileage;
    /** 近90天准时率 % */
    private BigDecimal ontimeRate;
    /** 最近保养日期 */
    private String lastMaintain;
    /** 备注 */
    private String remark;
    /** 逻辑删除 */
    private Integer deleted;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
    /** 乐观锁版本号 */
    private Integer version;
}
