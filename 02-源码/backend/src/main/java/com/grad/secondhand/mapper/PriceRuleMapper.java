package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.PriceRule;
import org.apache.ibatis.annotations.Mapper;

/** 定价评分规则版本 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface PriceRuleMapper extends BaseMapper<PriceRule> {
}
