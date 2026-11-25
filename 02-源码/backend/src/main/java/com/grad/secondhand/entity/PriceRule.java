package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 定价评分规则版本（表 price_rule） */
@Data
@TableName("price_rule")
public class PriceRule {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 规则名称 */
    private String ruleName;
    /** 规则版本号，如 v1/v2 */
    private String ruleVersion;
    /** 同分类成交价偏离度权重% */
    private Integer weightCategory;
    /** 成色权重% */
    private Integer weightCondition;
    /** 同校同类在售数量权重% */
    private Integer weightSupply;
    /** 发布时长与浏览转化权重% */
    private Integer weightHeat;
    /** 卖家信用分权重% */
    private Integer weightCredit;
    /** 图文完整度权重% */
    private Integer weightInfo;
    /** 草稿/启用/停用 */
    private String status;
    /** 生效时间 */
    private String effectTime;
    /** 变更说明 */
    private String remark;
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
