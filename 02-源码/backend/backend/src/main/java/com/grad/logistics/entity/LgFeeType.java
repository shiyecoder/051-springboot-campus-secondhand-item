package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 费用类型 */
@Data
@TableName("lg_fee_type")
public class LgFeeType {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 编码 */
    private String typeCode;
    /** 名称 */
    private String typeName;
    /** 方向 RECV应收/PAY应付 */
    private String direction;
    /** 默认单价（元/公里或元/吨） */
    private BigDecimal defaultRate;
    /** 状态 1启用 0停用 */
    private Integer status;
    /** 说明 */
    private String remark;
    /** 创建时间 */
    private String createTime;
}
