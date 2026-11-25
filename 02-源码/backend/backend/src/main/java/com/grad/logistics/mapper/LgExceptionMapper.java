package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgException;
import org.apache.ibatis.annotations.Mapper;

/** 异常事件 Mapper */
@Mapper
public interface LgExceptionMapper extends BaseMapper<LgException> {
}
