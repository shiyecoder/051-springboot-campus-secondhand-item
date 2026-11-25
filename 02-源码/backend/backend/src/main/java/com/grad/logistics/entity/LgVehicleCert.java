package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 车辆证件 */
@Data
@TableName("lg_vehicle_cert")
public class LgVehicleCert {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 车辆ID */
    private Long vehicleId;
    /** 车牌号 */
    private String plateNo;
    /** 证件类型 行驶证/年检/商业保险/营运证 */
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
