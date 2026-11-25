package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 配车评分明细 */
@Data
@TableName("lg_score_item")
public class LgScoreItem {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 评分ID */
    private Long scoreId;
    /** 评分维度 */
    private String dimension;
    /** 计算依据 */
    private String basis;
    /** 单项满分 */
    private Integer fullScore;
    /** 权重 % */
    private Integer weight;
    /** 加权得分 */
    private BigDecimal weightedScore;
    /** 创建时间 */
    private String createTime;
}
