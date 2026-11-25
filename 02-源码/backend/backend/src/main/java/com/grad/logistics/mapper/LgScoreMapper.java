package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgScore;
import org.apache.ibatis.annotations.Mapper;

/** 配车评分与计算快照 Mapper */
@Mapper
public interface LgScoreMapper extends BaseMapper<LgScore> {
}
