package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 驾驶员证件 */
@Data
@TableName("lg_driver_cert")
public class LgDriverCert {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 驾驶员ID */
    private Long driverId;
    /** 姓名（冗余） */
    private String driverName;
    /** 证件类型 驾驶证/从业资格证/体检记录 */
    private String certType;
    /** 证件编号 */
    private String certNo;
    /** 发证机关 */
    private String issuer;
    /** 生效日期 */
    private String issueDate;
    /** 到期日期 */
    private String expireDate;
    /** 证件附件 */
    private String filePath;
    /** 状态 VALID/WARN/EXPIRED */
    private String status;
    /** 创建时间 */
    private String createTime;
}
