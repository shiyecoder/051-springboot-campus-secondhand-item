package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgDemandItem;
import org.apache.ibatis.annotations.Mapper;

/** 需求货物明细 Mapper */
@Mapper
public interface LgDemandItemMapper extends BaseMapper<LgDemandItem> {
}
