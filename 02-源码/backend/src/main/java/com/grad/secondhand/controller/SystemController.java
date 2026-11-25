package com.grad.secondhand.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.PageResult;
import com.grad.secondhand.common.R;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.entity.*;
import com.grad.secondhand.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 系统管理：用户、角色、菜单、角色授权、字典、公告、消息与操作日志。
 * 用户接口不返回密码字段；分配菜单时过滤掉「菜单管理」自身，避免越权扩展权限树。
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
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final SysNoticeMapper noticeMapper;
    private final SysMessageMapper messageMapper;
    private final SysOperLogMapper logMapper;
    private final PasswordEncoder encoder;

    // ---------------------------------------------------------------- 用户
    /** 用户分页：密码字段不下发；学生账号附带学号与校区 */
    @GetMapping("/users")
    public R<PageResult<Map<String, Object>>> users(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String role_code,
                                                    @RequestParam(required = false) Integer status) {
        var w = Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            w.and(x -> x.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getStudentNo, keyword));
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
            m.put("student_no", u.getStudentNo());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("avatar", u.getAvatar());
            m.put("campus_id", u.getCampusId());
            m.put("campus_name", u.getCampusName());
            m.put("credit_score", u.getCreditScore());
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

    /** 新增/编辑用户：新增必填用户名，密码 BCrypt 保存 */
    @PostMapping("/users")
    public R<Void> saveUser(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        String username = (String) body.get("username");
        String realName = (String) body.get("real_name");
        if (username == null || username.isBlank()) {
            throw new BizException(4000, "用户名不能为空");
        }
        if (realName == null || realName.isBlank()) {
            throw new BizException(4000, "姓名不能为空");
        }
        SysUser u = new SysUser();
        u.setUsername(username.trim());
        u.setRealName(realName.trim());
        u.setStudentNo((String) body.get("student_no"));
        u.setPhone((String) body.get("phone"));
        u.setEmail((String) body.get("email"));
        u.setAvatar((String) body.get("avatar"));
        u.setGender((String) body.get("gender"));
        if (body.get("campus_id") != null) {
            u.setCampusId(Integer.valueOf(body.get("campus_id").toString()));
        }
        u.setCampusName((String) body.get("campus_name"));
        u.setStatus(body.get("status") == null ? 1 : Integer.valueOf(body.get("status").toString()));
        u.setUpdateTime(LocalDateTime.now().format(FMT));
        if (idObj == null) {
            Long dup = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, u.getUsername()));
            if (dup != null && dup > 0) {
                throw new BizException(4009, "用户名已存在");
            }
            String pwd = body.get("password") == null ? "123456" : body.get("password").toString();
            u.setPassword(encoder.encode(pwd));
            u.setCreditScore(100);
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
            ur.setCreateTime(LocalDateTime.now().format(FMT));
            ur.setDeleted(0);
            ur.setVersion(0);
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

    // ---------------------------------------------------------------- 角色与菜单
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

    /** 为角色分配菜单：先清后插。传入的菜单项会过滤掉「菜单管理」，防止越权自我扩权 */
    @PutMapping("/roles/{id}/menus")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        if (!UserContext.isAdmin()) {
            throw new BizException(4030, "只有平台管理员可以分配菜单");
        }
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, id));
        if (menuIds != null) {
            for (Long mid : menuIds) {
                SysMenu m = menuMapper.selectById(mid);
                if (m != null && "/admin/menus".equals(m.getPath())) {
                    continue;
                }
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(id);
                rm.setMenuId(mid);
                rm.setCreateTime(LocalDateTime.now().format(FMT));
                rm.setDeleted(0);
                rm.setVersion(0);
                roleMenuMapper.insert(rm);
            }
        }
        return R.ok();
    }

    /** 菜单树（全量，供管理页展示） */
    @GetMapping("/menus")
    public R<List<Map<String, Object>>> menus() {
        List<SysMenu> all = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSort));
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
        if (!UserContext.isAdmin()) {
            throw new BizException(4030, "只有平台管理员可以维护菜单");
        }
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
        m.setUpdateTime(LocalDateTime.now().format(FMT));
        if (id == null) {
            m.setCreateTime(LocalDateTime.now().format(FMT));
            m.setDeleted(0);
            m.setVersion(0);
            menuMapper.insert(m);
        } else {
            m.setId(Long.valueOf(id.toString()));
            if (m.getParentId().equals(m.getId())) {
                throw new BizException(4000, "上级菜单不能选择自身");
            }
            menuMapper.updateById(m);
        }
        return R.ok();
    }

    @DeleteMapping("/menus/{id}")
    public R<Void> deleteMenu(@PathVariable Long id) {
        if (!UserContext.isAdmin()) {
            throw new BizException(4030, "只有平台管理员可以删除菜单");
        }
        Long child = menuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, id));
        if (child != null && child > 0) {
            throw new BizException(4001, "存在子菜单，请先删除子菜单");
        }
        menuMapper.deleteById(id);
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, id));
        return R.ok();
    }

    // ---------------------------------------------------------------- 字典
    @GetMapping("/dicts/types")
    public R<List<SysDictType>> dictTypes() {
        return R.ok(dictTypeMapper.selectList(Wrappers.<SysDictType>lambdaQuery()
                .eq(SysDictType::getStatus, 1).orderByAsc(SysDictType::getId)));
    }

    /** 字典项：前端下拉统一从这里取值，避免前后端各写一套枚举 */
    @GetMapping("/dicts")
    public R<List<SysDictData>> dicts(@RequestParam(required = false) String type) {
        var w = Wrappers.<SysDictData>lambdaQuery().eq(SysDictData::getStatus, 1).orderByAsc(SysDictData::getSort);
        if (type != null && !type.isBlank()) {
            w.eq(SysDictData::getDictType, type);
        }
        return R.ok(dictDataMapper.selectList(w));
    }

    // ---------------------------------------------------------------- 公告 / 消息 / 日志
    @GetMapping("/notices")
    public R<PageResult<SysNotice>> notices(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size) {
        var p = noticeMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size),
                Wrappers.<SysNotice>lambdaQuery().eq(SysNotice::getDeleted, 0).orderByDesc(SysNotice::getId));
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    @PostMapping("/notices")
    public R<Void> saveNotice(@RequestBody Map<String, Object> body) {
        if (!UserContext.isOperator()) {
            throw new BizException(4030, "只有运营人员可以发布公告");
        }
        SysNotice n = new SysNotice();
        n.setTitle((String) body.get("title"));
        n.setContent((String) body.get("content"));
        n.setNoticeType(body.get("notice_type") == null ? "平台公告" : body.get("notice_type").toString());
        n.setPublisher(UserContext.realName());
        n.setPublishTime(LocalDateTime.now().format(FMT));
        n.setStatus(body.get("status") == null ? "已发布" : body.get("status").toString());
        n.setCreateTime(LocalDateTime.now().format(FMT));
        n.setDeleted(0);
        n.setVersion(0);
        noticeMapper.insert(n);
        return R.ok();
    }

    @PutMapping("/notices/{id}")
    public R<Void> updateNotice(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysNotice n = new SysNotice();
        n.setId(id);
        n.setTitle((String) body.get("title"));
        n.setContent((String) body.get("content"));
        n.setNoticeType(body.get("notice_type") == null ? null : body.get("notice_type").toString());
        n.setStatus(body.get("status") == null ? null : body.get("status").toString());
        noticeMapper.updateById(n);
        return R.ok();
    }

    /** 我的消息 */
    @GetMapping("/messages")
    public R<List<SysMessage>> messages(@RequestParam(required = false) Integer unread) {
        var w = Wrappers.<SysMessage>lambdaQuery().eq(SysMessage::getReceiverId, UserContext.userId())
                .orderByDesc(SysMessage::getId).last("limit 50");
        if (unread != null && unread == 1) {
            w = Wrappers.<SysMessage>lambdaQuery().eq(SysMessage::getReceiverId, UserContext.userId())
                    .eq(SysMessage::getReadFlag, 0).orderByDesc(SysMessage::getId).last("limit 50");
        }
        return R.ok(messageMapper.selectList(w));
    }

    @PutMapping("/messages/{id}/read")
    public R<Void> readMsg(@PathVariable Long id) {
        SysMessage m = new SysMessage();
        m.setId(id);
        m.setReadFlag(1);
        messageMapper.updateById(m);
        return R.ok();
    }

    /** 全部标为已读 */
    @PutMapping("/messages/read-all")
    public R<Void> readAll() {
        for (SysMessage m : messageMapper.selectList(Wrappers.<SysMessage>lambdaQuery()
                .eq(SysMessage::getReceiverId, UserContext.userId()).eq(SysMessage::getReadFlag, 0))) {
            SysMessage u = new SysMessage();
            u.setId(m.getId());
            u.setReadFlag(1);
            messageMapper.updateById(u);
        }
        return R.ok();
    }

    /** 操作日志：不可删除（需求 5.8），只提供查询 */
    @GetMapping("/logs")
    public R<PageResult<SysOperLog>> logs(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String module,
                                          @RequestParam(required = false) String oper_name) {
        var w = Wrappers.<SysOperLog>lambdaQuery().orderByDesc(SysOperLog::getId);
        if (module != null && !module.isBlank()) {
            w.like(SysOperLog::getModule, module);
        }
        if (oper_name != null && !oper_name.isBlank()) {
            w.like(SysOperLog::getOperName, oper_name);
        }
        var p = logMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), w);
        return R.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }
}
