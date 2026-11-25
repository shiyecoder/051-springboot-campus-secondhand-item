package com.grad.logistics.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.logistics.common.BizException;
import com.grad.logistics.common.PageResult;
import com.grad.logistics.common.R;
import com.grad.logistics.common.UserContext;
import com.grad.logistics.entity.*;
import com.grad.logistics.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 系统管理：用户、角色、菜单、角色授权、字典、消息与日志。
 * 用户接口一律不返回密码字段；菜单管理与分配菜单中的「菜单管理」选项按规范隐藏。
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysDictMapper dictMapper;
    private final SysMessageMapper messageMapper;
    private final SysLogMapper logMapper;
    private final SysOrgMapper orgMapper;
    private final PasswordEncoder encoder;

    /** 用户分页，密码字段不返回 */
    @GetMapping("/users")
    public R<PageResult<Map<String, Object>>> users(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String role_code,
                                                    @RequestParam(required = false) Integer status) {
        var w = Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            w.and(x -> x.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        }
        if (status != null) {
            w.eq(SysUser::getStatus, status);
        }
        w.orderByAsc(SysUser::getId);
        var p = userMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), w);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SysUser u : p.getRecords()) {
            SysRole r = roleOf(u.getId());
            if (role_code != null && !role_code.isBlank() && (r == null || !role_code.equals(r.getRoleCode()))) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("real_name", u.getRealName());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("org_id", u.getOrgId());
            m.put("org_name", u.getOrgName());
            m.put("avatar", u.getAvatar());
            m.put("gender", u.getGender());
            m.put("status", u.getStatus());
            m.put("last_login", u.getLastLogin());
            m.put("role_code", r == null ? null : r.getRoleCode());
            m.put("role_name", r == null ? null : r.getRoleName());
            rows.add(m);
        }
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), rows));
    }

    private SysRole roleOf(Long userId) {
        SysUserRole ur = userRoleMapper.selectOne(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId).last("limit 1"));
        return ur == null ? null : roleMapper.selectById(ur.getRoleId());
    }

    /** 新增/编辑用户：新增必填用户名与密码，密码 BCrypt 保存 */
    @PostMapping("/users")
    public R<Void> saveUser(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        String username = (String) body.get("username");
        String realName = (String) body.get("real_name");
        if (username == null || username.isBlank()) {
            throw new BizException(400, "用户名不能为空");
        }
        if (realName == null || realName.isBlank()) {
            throw new BizException(400, "姓名不能为空");
        }
        SysUser u = new SysUser();
        u.setUsername(username.trim());
        u.setRealName(realName.trim());
        u.setPhone((String) body.get("phone"));
        u.setEmail((String) body.get("email"));
        u.setAvatar((String) body.get("avatar"));
        u.setGender((String) body.get("gender"));
        Object orgId = body.get("org_id");
        if (orgId != null) {
            u.setOrgId(Long.valueOf(orgId.toString()));
            SysOrg org = orgMapper.selectById(u.getOrgId());
            u.setOrgName(org == null ? null : org.getOrgName());
        }
        u.setStatus(body.get("status") == null ? 1 : Integer.valueOf(body.get("status").toString()));
        u.setUpdateTime(LocalDateTime.now().format(FMT));
        if (idObj == null) {
            Long dup = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, u.getUsername()));
            if (dup != null && dup > 0) {
                throw new BizException(400, "用户名已存在");
            }
            String pwd = body.get("password") == null ? "123456" : body.get("password").toString();
            u.setPassword(encoder.encode(pwd));
            u.setLoginFailCount(0);
            u.setDeleted(0);
            u.setCreateTime(LocalDateTime.now().format(FMT));
            userMapper.insert(u);
            bindRole(u.getId(), body.get("role_code"));
        } else {
            u.setId(Long.valueOf(idObj.toString()));
            if (body.get("password") != null && !body.get("password").toString().isBlank()) {
                u.setPassword(encoder.encode(body.get("password").toString()));
            }
            userMapper.updateById(u);
            if (body.get("role_code") != null) {
                userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, u.getId()));
                bindRole(u.getId(), body.get("role_code"));
            }
        }
        return R.ok();
    }

    private void bindRole(Long userId, Object roleCode) {
        if (roleCode == null) {
            return;
        }
        SysRole r = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, roleCode.toString()));
        if (r != null) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(r.getId());
            userRoleMapper.insert(ur);
        }
    }

    /** 重置密码为 123456 */
    @PutMapping("/users/{id}/reset")
    public R<Void> resetPwd(@PathVariable Long id) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setPassword(encoder.encode("123456"));
        u.setLoginFailCount(0);
        u.setLockUntil(null);
        userMapper.updateById(u);
        return R.ok();
    }

    @PutMapping("/users/{id}/status")
    public R<Void> toggle(@PathVariable Long id, @RequestParam Integer status) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setStatus(status);
        userMapper.updateById(u);
        return R.ok();
    }

    @GetMapping("/roles")
    public R<List<Map<String, Object>>> roles() {
        List<SysRole> roles = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery().orderByAsc(SysRole::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysRole r : roles) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("role_code", r.getRoleCode());
            m.put("role_name", r.getRoleName());
            m.put("data_scope", r.getDataScope());
            m.put("remark", r.getRemark());
            m.put("status", r.getStatus());
            m.put("menu_count", roleMenuMapper.selectCount(
                    Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, r.getId())));
            m.put("user_count", userRoleMapper.selectCount(
                    Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, r.getId())));
            list.add(m);
        }
        return R.ok(list);
    }

    /** 角色已授权菜单 ID（分配菜单弹窗回显） */
    @GetMapping("/roles/{id}/menus")
    public R<List<Long>> roleMenus(@PathVariable Long id) {
        List<Long> ids = new ArrayList<>();
        for (SysRoleMenu rm : roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                .eq(SysRoleMenu::getRoleId, id))) {
            ids.add(rm.getMenuId());
        }
        return R.ok(ids);
    }

    /** 为角色分配菜单：先清后插，整表事务 */
    @PutMapping("/roles/{id}/menus")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        if (!UserContext.isManager()) {
            throw new BizException(403, "只有车队经理可以分配菜单");
        }
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, id));
        if (menuIds != null) {
            for (Long mid : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(id);
                rm.setMenuId(mid);
                roleMenuMapper.insert(rm);
            }
        }
        return R.ok();
    }

    /** 菜单树（隐藏「菜单管理」项，按规范 12） */
    @GetMapping("/menus")
    public R<List<Map<String, Object>>> menus() {
        List<SysMenu> all = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .orderByAsc(SysMenu::getSort));
        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysMenu m : all) {
            if (m.getParentId() != null && m.getParentId() == 0) {
                tree.add(node(m, all));
            }
        }
        return R.ok(tree);
    }

    private Map<String, Object> node(SysMenu m, List<SysMenu> all) {
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("id", m.getId());
        n.put("parent_id", m.getParentId());
        n.put("menu_name", m.getMenuName());
        n.put("menu_type", m.getMenuType());
        n.put("path", m.getPath());
        n.put("perm", m.getPerm());
        n.put("icon", m.getIcon());
        n.put("sort", m.getSort());
        n.put("visible", m.getVisible());
        List<Map<String, Object>> children = new ArrayList<>();
        for (SysMenu c : all) {
            if (c.getParentId() != null && c.getParentId().equals(m.getId())) {
                children.add(node(c, all));
            }
        }
        if (!children.isEmpty()) {
            n.put("children", children);
        }
        return n;
    }

    @PostMapping("/menus")
    public R<Void> saveMenu(@RequestBody Map<String, Object> body) {
        SysMenu m = new SysMenu();
        Object id = body.get("id");
        m.setParentId(body.get("parent_id") == null ? 0L : Long.valueOf(body.get("parent_id").toString()));
        m.setMenuName((String) body.get("menu_name"));
        m.setMenuType(body.get("menu_type") == null ? "menu" : body.get("menu_type").toString());
        m.setPath((String) body.get("path"));
        m.setPerm((String) body.get("perm"));
        m.setIcon((String) body.get("icon"));
        m.setSort(body.get("sort") == null ? 0 : Integer.valueOf(body.get("sort").toString()));
        m.setVisible(body.get("visible") == null ? 1 : Integer.valueOf(body.get("visible").toString()));
        if (id == null) {
            m.setCreateTime(LocalDateTime.now().format(FMT));
            menuMapper.insert(m);
        } else {
            m.setId(Long.valueOf(id.toString()));
            if (m.getParentId().equals(m.getId())) {
                throw new BizException(400, "上级菜单不能选择自身");
            }
            menuMapper.updateById(m);
        }
        return R.ok();
    }

    @DeleteMapping("/menus/{id}")
    public R<Void> deleteMenu(@PathVariable Long id) {
        Long child = menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id));
        if (child != null && child > 0) {
            throw new BizException(400, "存在子菜单，请先删除子菜单");
        }
        menuMapper.deleteById(id);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, id));
        return R.ok();
    }

    @GetMapping("/dicts")
    public R<List<SysDict>> dicts(@RequestParam(required = false) String type) {
        var w = Wrappers.<SysDict>lambdaQuery().eq(SysDict::getStatus, 1).orderByAsc(SysDict::getSort);
        if (type != null && !type.isBlank()) {
            w.eq(SysDict::getDictType, type);
        }
        return R.ok(dictMapper.selectList(w));
    }

    @GetMapping("/orgs")
    public R<List<SysOrg>> orgs() {
        return R.ok(orgMapper.selectList(Wrappers.<SysOrg>lambdaQuery().orderByAsc(SysOrg::getSort)));
    }

    /** 我的消息（广播 + 指定接收人），并按角色过滤 */
    @GetMapping("/messages")
    public R<List<SysMessage>> messages() {
        return R.ok(messageMapper.selectList(Wrappers.<SysMessage>lambdaQuery()
                .and(x -> x.isNull(SysMessage::getReceiver)
                        .or().eq(SysMessage::getReceiver, UserContext.username())
                        .or().eq(SysMessage::getReceiver, UserContext.roleCode()))
                .orderByDesc(SysMessage::getId).last("limit 50")));
    }

    @PutMapping("/messages/{id}/read")
    public R<Void> readMsg(@PathVariable Long id) {
        SysMessage m = new SysMessage();
        m.setId(id);
        m.setIsRead(1);
        messageMapper.updateById(m);
        return R.ok();
    }

    @GetMapping("/logs")
    public R<PageResult<SysLog>> logs(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String username,
                                      @RequestParam(required = false) String action) {
        var w = Wrappers.<SysLog>lambdaQuery().orderByDesc(SysLog::getId);
        if (username != null && !username.isBlank()) {
            w.like(SysLog::getUsername, username);
        }
        if (action != null && !action.isBlank()) {
            w.like(SysLog::getAction, action);
        }
        var p = logMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }
}
