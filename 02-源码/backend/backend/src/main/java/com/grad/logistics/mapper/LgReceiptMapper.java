package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgReceipt;
import org.apache.ibatis.annotations.Mapper;

/** 回单与签收凭证 Mapper */
@Mapper
public interface LgReceiptMapper extends BaseMapper<LgReceipt> {
}
