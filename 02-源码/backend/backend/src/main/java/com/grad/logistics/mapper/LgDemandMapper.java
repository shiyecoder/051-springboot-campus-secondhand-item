package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgDemand;
import org.apache.ibatis.annotations.Mapper;

/** 运输需求 Mapper */
@Mapper
public interface LgDemandMapper extends BaseMapper<LgDemand> {
}
