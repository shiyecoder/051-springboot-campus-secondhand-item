package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.CampusPlace;
import org.apache.ibatis.annotations.Mapper;

/** 校区面交点 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface CampusPlaceMapper extends BaseMapper<CampusPlace> {
}
