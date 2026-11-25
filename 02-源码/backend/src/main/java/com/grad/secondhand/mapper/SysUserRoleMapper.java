package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/** 用户角色 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
