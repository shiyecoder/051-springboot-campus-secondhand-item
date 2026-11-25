package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 运输节点 */
@Data
@TableName("lg_node")
public class LgNode {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 节点编号 */
    private String nodeNo;
    /** 运单ID */
    private Long orderId;
    /** 运单编号（冗余） */
    private String orderNo;
    /** 节点类型 LOAD/DEPART/TRANSIT/ARRIVE/UNLOAD/SIGN */
    private String nodeType;
    /** 节点名称 */
    private String nodeName;
    /** 地点文字 */
    private String place;
    /** 发生时间 */
    private String nodeTime;
    /** 说明 */
    private String description;
    /** 节点图片 */
    private String images;
    /** 上报人 */
    private String reporter;
    /** 创建时间 */
    private String createTime;
}
