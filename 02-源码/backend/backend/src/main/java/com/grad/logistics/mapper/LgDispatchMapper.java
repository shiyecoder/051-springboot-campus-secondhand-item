package com.grad.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.logistics.entity.LgDispatch;
import org.apache.ibatis.annotations.Mapper;

/** 车辆调度任务 Mapper */
@Mapper
public interface LgDispatchMapper extends BaseMapper<LgDispatch> {
}
