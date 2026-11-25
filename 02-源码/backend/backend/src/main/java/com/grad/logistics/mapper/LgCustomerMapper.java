package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgCustomer;
import org.apache.ibatis.annotations.Mapper;

/** 客户档案 Mapper */
@Mapper
public interface LgCustomerMapper extends BaseMapper<LgCustomer> {
}
