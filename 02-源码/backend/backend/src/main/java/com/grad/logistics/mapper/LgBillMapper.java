package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgBill;
import org.apache.ibatis.annotations.Mapper;

/** 客户账单 Mapper */
@Mapper
public interface LgBillMapper extends BaseMapper<LgBill> {
}
