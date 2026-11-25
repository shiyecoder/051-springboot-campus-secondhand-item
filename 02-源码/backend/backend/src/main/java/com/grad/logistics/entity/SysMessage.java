package com.grad.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 站内消息 */
@Data
@TableName("sys_message")
public class SysMessage {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 类型 待审批/即将超时/已退回/待验收 */
    private String msgType;
    /** 标题 */
    private String title;
    /** 业务编号 */
    private String bizNo;
    /** 详情跳转地址 */
    private String bizPath;
    /** 接收人（空为广播） */
    private String receiver;
    /** 是否已读 */
    private Integer isRead;
    /** 创建时间 */
    private String createTime;
}
