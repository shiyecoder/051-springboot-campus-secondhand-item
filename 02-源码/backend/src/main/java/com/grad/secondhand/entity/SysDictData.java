package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 字典数据（表 sys_dict_data） */
@Data
@TableName("sys_dict_data")
public class SysDictData {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 字典类型编码 */
    private String dictType;
    /** 字典标签 */
    private String dictLabel;
    /** 字典值 */
    private String dictValue;
    /** 排序号 */
    private Integer sort;
    /** 1启用 0停用 */
    private Integer status;
    /** 备注 */
    private String remark;
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
