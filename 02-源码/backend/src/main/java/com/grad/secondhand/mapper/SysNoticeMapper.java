package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;

/** 公告 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
}
