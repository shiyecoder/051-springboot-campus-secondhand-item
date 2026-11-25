package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 校区面交点（表 campus_place） */
@Data
@TableName("campus_place")
public class CampusPlace {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 校区编号1东2西3北 */
    private Integer campusId;
    /** 校区名称 */
    private String campusName;
    /** 面交点名称 */
    private String placeName;
    /** 位置说明 */
    private String addressDesc;
    /** 开放开始时间 */
    private String openStart;
    /** 开放结束时间 */
    private String openEnd;
    /** 现场联系人，仅运营可见 */
    private String managerName;
    /** 联系电话，前台脱敏 */
    private String managerPhone;
    /** 1启用 0停用 */
    private Integer status;
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
