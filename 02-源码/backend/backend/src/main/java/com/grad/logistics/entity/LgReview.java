package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 服务评价 */
@Data
@TableName("lg_review")
public class LgReview {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 评价编号 */
    private String reviewNo;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 客户（冗余） */
    private String customerName;
    /** 评分 1-5 */
    private Integer score;
    /** 评价内容 */
    private String content;
    /** 标签 */
    private String tags;
    /** 状态 NORMAL/ABNORMAL */
    private String status;
    /** 评价时间 */
    private String reviewTime;
    /** 创建时间 */
    private String createTime;
}
