package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/** 举报与纠纷 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
