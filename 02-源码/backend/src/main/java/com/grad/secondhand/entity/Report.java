package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 举报与纠纷（表 report） */
@Data
@TableName("report")
public class Report {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** JB+yyMMdd+4位序号 */
    private String reportNo;
    /** 商品/订单/用户 */
    private String bizType;
    /** 按类型关联 goods/trade_order/sys_user */
    private Long bizId;
    /** 关联对象标题快照 */
    private String bizTitle;
    /** 举报人 */
    private Long reporterId;
    /** 违禁品/虚假描述/诈骗/骚扰/校外人员 */
    private String reasonType;
    /** 问题描述，不少于10字 */
    private String description;
    /** 证据图片，最多6张 */
    private String evidenceUrls;
    /** 待受理/处理中/已处理/已驳回/申诉中 */
    private String status;
    /** 处理人 */
    private Long handlerId;
    /** 处理人姓名 */
    private String handlerName;
    /** 处理结果 */
    private String handleResult;
    /** 无/申诉中/申诉通过/申诉驳回 */
    private String appealStatus;
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
