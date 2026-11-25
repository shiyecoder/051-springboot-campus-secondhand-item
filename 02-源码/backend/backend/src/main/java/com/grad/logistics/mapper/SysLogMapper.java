package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/** 操作日志 Mapper */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
}
