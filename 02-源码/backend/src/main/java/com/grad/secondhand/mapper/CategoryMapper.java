package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/** 商品分类 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
