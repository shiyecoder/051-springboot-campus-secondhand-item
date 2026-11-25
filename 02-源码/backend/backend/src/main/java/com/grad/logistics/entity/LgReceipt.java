package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 回单与签收凭证 */
@Data
@TableName("lg_receipt")
public class LgReceipt {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 回单编号 */
    private String receiptNo;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 上传人 */
    private String uploader;
    /** 凭证张数 */
    private Integer fileCount;
    /** 凭证图片 */
    private String images;
    /** 状态 PENDING/REJECTED/CHECKED/ARCHIVED */
    private String status;
    /** 退回原因 */
    private String rejectReason;
    /** 纸质回单 NONE/MAILED/RECEIVED/ARCHIVED */
    private String paperStatus;
    /** 存放位置 */
    private String archiveLocation;
    /** 复核人 */
    private String checkBy;
    /** 复核时间 */
    private String checkTime;
    /** 上传时间 */
    private String uploadTime;
    /** 创建时间 */
    private String createTime;
}
