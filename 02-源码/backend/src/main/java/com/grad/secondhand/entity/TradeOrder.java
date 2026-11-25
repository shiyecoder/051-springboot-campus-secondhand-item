package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 交易订单（表 trade_order） */
@Data
@TableName("trade_order")
public class TradeOrder {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** DD+yyMMdd+6位序号 */
    private String orderNo;
    /** 商品 goods.id */
    private Long goodsId;
    /** 商品标题快照 */
    private String goodsTitle;
    /** 商品封面快照 */
    private String goodsCover;
    /** 买家 sys_user.id */
    private Long buyerId;
    /** 卖家，下单时快照 */
    private Long sellerId;
    /** 成交价，卖家确认时填写 */
    private BigDecimal dealPrice;
    /** 现金/转账/其他 */
    private String payType;
    /** 面交时间 */
    private String appointTime;
    /** 面交点 */
    private String appointPlace;
    /** 待卖家确认/待面交/已完成/已取消/纠纷中 */
    private String status;
    /** 取消原因 */
    private String cancelReason;
    /** 买家确认收货时间 */
    private String buyerConfirmTime;
    /** 卖家接单时间 */
    private String sellerConfirmTime;
    /** 商品锁定到期时间 */
    private String lockExpireTime;
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
