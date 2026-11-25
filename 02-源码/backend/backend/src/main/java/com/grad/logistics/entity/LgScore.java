package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 配车评分与计算快照 */
@Data
@TableName("lg_score")
public class LgScore {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 规则ID */
    private Long ruleId;
    /** 规则版本 */
    private String ruleVersion;
    /** 输入快照 JSON */
    private String inputSnapshot;
    /** 总分 */
    private Integer totalScore;
    /** 结果等级 RECOMMEND/BACKUP/REJECT */
    private String resultLevel;
    /** 推荐车辆ID */
    private Long vehicleId;
    /** 车牌号 */
    private String plateNo;
    /** 推荐驾驶员ID */
    private Long driverId;
    /** 驾驶员 */
    private String driverName;
    /** 主要影响因素 */
    private String mainFactors;
    /** 未满足条件 */
    private String unmetConditions;
    /** 可改进项 */
    private String improvements;
    /** 是否人工修正 */
    private Integer manualFlag;
    /** 人工修正车辆 */
    private String manualVehicle;
    /** 修正原因 */
    private String manualReason;
    /** 修正人 */
    private String manualBy;
    /** 修正时间 */
    private String manualTime;
    /** 计算时间 */
    private String genTime;
    /** 创建时间 */
    private String createTime;
}
