package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;

/** 字典类型 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {
}
