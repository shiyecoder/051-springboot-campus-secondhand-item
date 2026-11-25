package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 驾驶员档案 */
@Data
@TableName("lg_driver")
public class LgDriver {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 工号 */
    private String driverNo;
    /** 姓名 */
    private String name;
    /** 联系电话 */
    private String phone;
    /** 驾驶证类型 A2/B2 */
    private String licenseType;
    /** 驾驶证有效期 */
    private String licenseExpire;
    /** 从业资格证有效期 */
    private String certExpire;
    /** 所属车队 */
    private String fleetName;
    /** 头像（本地相对路径） */
    private String avatar;
    /** 状态 AVAILABLE/RUNNING/LEAVE */
    private String status;
    /** 近90天准时率 % */
    private BigDecimal ontimeRate;
    /** 累计任务数 */
    private Integer totalTrips;
    /** 关联账号 */
    private Long userId;
    /** 备注 */
    private String remark;
    /** 逻辑删除 */
    private Integer deleted;
    /** 创建时间 */
    private String createTime;
    /** 更新时间 */
    private String updateTime;
}
