package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

/** 角色 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
