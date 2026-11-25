package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 闲置商品（表 goods） */
@Data
@TableName("goods")
public class Goods {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** XY+yyMMdd+4位序号 */
    private String goodsNo;
    /** 卖家 sys_user.id */
    private Long sellerId;
    /** 二级分类 category.id */
    private Long categoryId;
    /** 标题 */
    private String title;
    /** 商品描述，10-1000字 */
    private String description;
    /** 全新/九成新/七成新/五成新/有瑕疵 */
    private String conditionLevel;
    /** 出售价 */
    private BigDecimal price;
    /** 入手价，用于折价率 */
    private BigDecimal originalPrice;
    /** 校区编号 */
    private Integer campusId;
    /** 校区名称（冗余便于列表展示） */
    private String campusName;
    /** 面交点 campus_place.id */
    private Long tradePlaceId;
    /** 面交/自提/可送 */
    private String tradeType;
    /** 待审核/在售/已下架/已售出/审核驳回/冻结 */
    private String status;
    /** 审核意见，驳回时必填 */
    private String auditRemark;
    /** 浏览量 */
    private Integer viewCount;
    /** 收藏量，用于热度评分 */
    private Integer favoriteCount;
    /** 封面图相对路径 */
    private String coverUrl;
    /** 实拍图，相对路径逗号分隔，最多6张 */
    private String imageUrls;
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
