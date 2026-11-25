package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 配车规则版本 */
@Data
@TableName("lg_rule")
public class LgRule {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 版本号 v1/v2/v3 */
    private String version;
    /** 各维度权重 JSON：load/volume/type/deadhead/timewin/ontime */
    private String weights;
    /** 推荐阈值 */
    private Integer thresholdRecommend;
    /** 备选阈值 */
    private Integer thresholdBackup;
    /** 空驶上限 km */
    private Integer maxDeadheadKm;
    /** 任务最小间隔分钟 */
    private Integer minGapMinutes;
    /** 核载利用率上限 % */
    private Integer loadLimitRate;
    /** 容积利用率上限 % */
    private Integer volumeLimitRate;
    /** 状态 ACTIVE/ARCHIVED */
    private String status;
    /** 生效时间 */
    private String effectTime;
    /** 修改人 */
    private String createBy;
    /** 变更说明 */
    private String remark;
    /** 创建时间 */
    private String createTime;
}
