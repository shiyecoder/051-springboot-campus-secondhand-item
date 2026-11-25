package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 定价评分记录（表 price_calc） */
@Data
@TableName("price_calc")
public class PriceCalc {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 商品 goods.id */
    private Long goodsId;
    /** 规则 price_rule.id */
    private Long ruleId;
    /** 计算时使用的规则版本 */
    private String ruleVersion;
    /** 输入快照 JSON */
    private String snapshot;
    /** 分类价格偏离得分 */
    private BigDecimal scoreCategory;
    /** 成色得分 */
    private BigDecimal scoreCondition;
    /** 校区供需得分 */
    private BigDecimal scoreSupply;
    /** 热度得分 */
    private BigDecimal scoreHeat;
    /** 信用得分 */
    private BigDecimal scoreCredit;
    /** 图文完整度得分 */
    private BigDecimal scoreInfo;
    /** 加权总分 */
    private BigDecimal totalScore;
    /** A/B/C */
    private String grade;
    /** 建议价下限 */
    private BigDecimal suggestLow;
    /** 建议价上限 */
    private BigDecimal suggestHigh;
    /** 推荐结论 */
    private String conclusion;
    /** 主要影响因素 */
    private String factors;
    /** 未满足条件 */
    private String missing;
    /** 人工修正后的等级 */
    private String correctedGrade;
    /** 修正原因，不少于5字 */
    private String correctReason;
    /** 修正人 */
    private Long correctBy;
    /** 修正时间 */
    private String correctTime;
    /** 卖家是否采纳建议价 0未采纳1采纳 */
    private Integer adopted;
    /** 创建人 */
    private Long createBy;
    /** 创建时间 */
    private String createTime;
    /** 更新人 */
    private Long updateBy;
    /** 更新时间 */
    private String updateTime;
    /** 0正常 1删除 */
    private Integer deleted;
    /** 乐观锁版本号 */
    private Integer version;
}
