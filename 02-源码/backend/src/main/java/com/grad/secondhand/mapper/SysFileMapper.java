package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;

/** 文件元数据 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysFileMapper extends BaseMapper<SysFile> {
}
