package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/** 站内消息 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}
