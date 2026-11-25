package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 信用分流水（表 credit_log） */
@Data
@TableName("credit_log")
public class CreditLog {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 sys_user.id */
    private Long userId;
    /** 正数加分，负数扣分 */
    private Integer changeValue;
    /** 变化前分值 */
    private Integer beforeScore;
    /** 变化后分值 */
    private Integer afterScore;
    /** 交易好评/差评/放鸽子/违规下架/人工调整 */
    private String reasonType;
    /** 订单号或举报编号 */
    private String bizNo;
    /** 说明 */
    private String remark;
    /** 操作人，系统自动为空 */
    private Long operatorId;
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
