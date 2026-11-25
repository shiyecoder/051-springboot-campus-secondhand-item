package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgPayment;
import org.apache.ibatis.annotations.Mapper;

/** 线下收付款登记 Mapper */
@Mapper
public interface LgPaymentMapper extends BaseMapper<LgPayment> {
}
