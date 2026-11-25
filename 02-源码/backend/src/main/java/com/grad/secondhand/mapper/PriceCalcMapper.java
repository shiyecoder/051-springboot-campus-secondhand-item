package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.PriceCalc;
import org.apache.ibatis.annotations.Mapper;

/** 定价评分记录 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface PriceCalcMapper extends BaseMapper<PriceCalc> {
}
