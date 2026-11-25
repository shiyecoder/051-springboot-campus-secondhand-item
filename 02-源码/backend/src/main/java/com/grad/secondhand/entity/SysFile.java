package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 文件元数据（表 sys_file） */
@Data
@TableName("sys_file")
public class SysFile {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 原始文件名 */
    private String fileName;
    /** 相对路径 */
    private String filePath;
    /** 字节数 */
    private Long fileSize;
    /** 扩展名 */
    private String fileType;
    /** 业务类型 goods/report/avatar */
    private String bizType;
    /** 业务ID */
    private Long bizId;
    /** 上传人 */
    private Long uploaderId;
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
