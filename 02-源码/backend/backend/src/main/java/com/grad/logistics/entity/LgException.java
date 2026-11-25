package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 异常事件 */
@Data
@TableName("lg_exception")
public class LgException {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 异常编号 */
    private String exceptionNo;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 异常类型 堵车延误/车辆故障/货损货差/拒收/地址变更/延误 */
    private String type;
    /** 程度 LOW/MID/HIGH/SEVERE */
    private String level;
    /** 简要描述 */
    private String description;
    /** 责任人 */
    private String ownerName;
    /** 处理时限 */
    private String deadline;
    /** 处理过程 */
    private String processNote;
    /** 客户沟通记录 */
    private String customerNote;
    /** 现场照片 */
    private String images;
    /** 状态 OPEN/PROCESSING/PENDING_AUDIT/CLOSED */
    private String status;
    /** 审核人 */
    private String auditBy;
    /** 审核时间 */
    private String auditTime;
    /** 审核意见 */
    private String auditNote;
    /** 登记人 */
    private String reporter;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
}
