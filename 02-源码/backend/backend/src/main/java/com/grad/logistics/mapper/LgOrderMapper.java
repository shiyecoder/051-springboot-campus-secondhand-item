package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgOrder;
import org.apache.ibatis.annotations.Mapper;

/** 运单主档 Mapper */
@Mapper
public interface LgOrderMapper extends BaseMapper<LgOrder> {
}
