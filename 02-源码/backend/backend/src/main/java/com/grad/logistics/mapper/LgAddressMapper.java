package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgAddress;
import org.apache.ibatis.annotations.Mapper;

/** 客户常用地址 Mapper */
@Mapper
public interface LgAddressMapper extends BaseMapper<LgAddress> {
}
