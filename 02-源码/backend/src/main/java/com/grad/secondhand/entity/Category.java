package com.grad.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 商品分类（表 category） */
@Data
@TableName("category")
public class Category {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 0表示一级分类，最多两级 */
    private Long parentId;
    /** 分类编码，大写字母与下划线 */
    private String categoryCode;
    /** 分类名称 */
    private String name;
    /** 图标相对路径 */
    private String icon;
    /** 排序号，越小越靠前 */
    private Integer sortNo;
    /** 1启用 0停用 */
    private Integer status;
    /** 1首页热门 0普通 */
    private Integer isHot;
    /** 运营备注 */
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
