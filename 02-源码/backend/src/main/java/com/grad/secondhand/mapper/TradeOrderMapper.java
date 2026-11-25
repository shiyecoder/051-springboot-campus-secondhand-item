package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;

/** 交易订单 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {
}
