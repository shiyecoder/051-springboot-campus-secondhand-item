package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 站内消息（表 sys_message） */
@Data
@TableName("sys_message")
public class SysMessage {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 接收人 */
    private Long receiverId;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** goods/order/report/credit */
    private String bizType;
    /** 业务ID */
    private Long bizId;
    /** 审核/订单/纠纷/信用/系统通知 */
    private String msgType;
    /** 0未读 1已读 */
    private Integer readFlag;
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
