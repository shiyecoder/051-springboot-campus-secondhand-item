package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 交易评价（表 review） */
@Data
@TableName("review")
public class Review {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单 trade_order.id */
    private Long orderId;
    /** 评价人 */
    private Long fromUserId;
    /** 被评人 */
    private Long toUserId;
    /** 1至5星 */
    private Integer score;
    /** 描述一致/准时/好沟通/包装完好 */
    private String tags;
    /** 评价内容 */
    private String content;
    /** 0显示昵称 1匿名 */
    private Integer anonymous;
    /** 追评，30天内一次 */
    private String appendContent;
    /** 待审核/已展示/已隐藏 */
    private String status;
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
