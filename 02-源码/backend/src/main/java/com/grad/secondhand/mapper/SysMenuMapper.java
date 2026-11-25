package com.grad.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grad.secondhand.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/** 菜单权限 Mapper，通用 CRUD 由 BaseMapper 提供 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}
