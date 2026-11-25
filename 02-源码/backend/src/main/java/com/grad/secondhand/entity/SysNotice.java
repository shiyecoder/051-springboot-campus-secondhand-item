package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 公告（表 sys_notice） */
@Data
@TableName("sys_notice")
public class SysNotice {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 平台公告/交易提醒/规则变更 */
    private String noticeType;
    /** 发布时间 */
    private String publishTime;
    /** 发布人 */
    private String publisher;
    /** 草稿/已发布/已下线 */
    private String status;
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
