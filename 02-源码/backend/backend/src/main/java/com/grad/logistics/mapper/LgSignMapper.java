package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgSign;
import org.apache.ibatis.annotations.Mapper;

/** 签收记录 Mapper */
@Mapper
public interface LgSignMapper extends BaseMapper<LgSign> {
}
