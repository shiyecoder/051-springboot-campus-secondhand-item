package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 客户常用地址 */
@Data
@TableName("lg_address")
public class LgAddress {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 客户ID */
    private Long customerId;
    /** 客户名称（冗余便于列表） */
    private String customerName;
    /** 地址类型 SHIP/RECEIVE */
    private String addrType;
    /** 省 */
    private String province;
    /** 市 */
    private String city;
    /** 详细地址 */
    private String address;
    /** 现场联系人 */
    private String contact;
    /** 联系电话 */
    private String phone;
    /** 可装卸时间 */
    private String loadTime;
    /** 场地限制 */
    private String siteLimit;
    /** 是否默认 */
    private Integer isDefault;
    /** 逻辑删除 */
    private Integer deleted;
    /** 创建时间 */
    private String createTime;
}
