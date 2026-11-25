package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/** 角色菜单 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}
