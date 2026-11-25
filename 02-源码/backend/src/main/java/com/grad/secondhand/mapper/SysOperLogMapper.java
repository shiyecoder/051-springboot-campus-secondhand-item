package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/** 操作日志 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
