package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

/** 闲置商品 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
