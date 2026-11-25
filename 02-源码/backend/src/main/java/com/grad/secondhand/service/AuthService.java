package com.grad.secondhand.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.grad.secondhand.common.BizException;
import com.grad.secondhand.common.JwtUtil;
import com.grad.secondhand.common.UserContext;
import com.grad.secondhand.dto.Dtos;
import com.grad.secondhand.entity.*;
import com.grad.secondhand.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证与个人中心：密码 BCrypt、连续失败锁定、JWT 签发、按角色下发菜单树。
 * 菜单只决定前端可见入口，接口权限仍由各写接口二次校验（前端隐藏按钮不等于授权）。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final TraceService trace;

    @Value("${app.login-fail-limit:5}")
    private int failLimit;

    @Value("${app.login-lock-minutes:10}")
    private int lockMinutes;

    /**
     * 登录：校验状态与密码、累计失败次数、签发 token，返回角色与菜单树。
     * 这里刻意不套 @Transactional：密码错误要先把失败次数加一并落库，再抛 BizException，
     * 若包在事务里，抛异常会把这次累加一起回滚，导致「连续失败 5 次锁定」永远触发不了。
     */
    public Map<String, Object> login(Dtos.LoginReq req, String ip) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, req.username().trim())
                .eq(SysUser::getDeleted, 0));
        if (user == null) {
            throw new BizException(4000, "登录名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(4030, "账号已停用，请联系校园运营人员");
        }
        if (user.getLockUntil() != null && !user.getLockUntil().isBlank()
                && user.getLockUntil().compareTo(LocalDateTime.now().format(FMT)) > 0) {
            throw new BizException(4030, "连续登录失败，账号已锁定至 " + user.getLockUntil());
        }
        if (!encoder.matches(req.password(), user.getPassword())) {
            int fail = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            SysUser upd = new SysUser();
            upd.setId(user.getId());
            upd.setLoginFailCount(fail);
            if (fail >= failLimit) {
                upd.setLockUntil(LocalDateTime.now().plusMinutes(lockMinutes).format(FMT));
                upd.setLoginFailCount(0);
            }
            userMapper.updateById(upd);
            throw new BizException(4000, fail >= failLimit
                    ? "连续失败 " + failLimit + " 次，账号已锁定 " + lockMinutes + " 分钟"
                    : "登录名或密码错误，还可尝试 " + (failLimit - fail) + " 次");
        }
        SysRole role = loadRole(user.getId());
        if (role == null) {
            throw new BizException(4030, "该账号未分配角色，请联系管理员");
        }
        SysUser ok = new SysUser();
        ok.setId(user.getId());
        ok.setLoginFailCount(0);
        ok.setLastLogin(LocalDateTime.now().format(FMT));
        userMapper.updateById(ok);

        UserContext.CurrentUser cu = new UserContext.CurrentUser(user.getId(), user.getUsername(),
                user.getRealName(), role.getRoleCode(), user.getDeptId(), user.getCampusName(), role.getDataScope());
        String token = jwtUtil.create(cu);
        trace.log("认证", "登录", "sys_user:" + user.getId(), "登录成功，IP " + ip);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("user", brief(user, role));
        data.put("menus", menuTree(role.getId()));
        return data;
    }

    /** 当前登录用户（刷新页面重新拉取，避免只依赖前端缓存） */
    public Map<String, Object> me() {
        SysUser user = userMapper.selectById(UserContext.userId());
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BizException(4010, "登录状态已失效，请重新登录");
        }
        SysRole role = loadRole(user.getId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", brief(user, role));
        data.put("menus", role == null ? List.of() : menuTree(role.getId()));
        return data;
    }

    /** 对外返回的用户信息：不含密码，附带信用分与校区 */
    private Map<String, Object> brief(SysUser u, SysRole r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("realName", u.getRealName());
        m.put("studentNo", u.getStudentNo());
        m.put("phone", u.getPhone());
        m.put("avatar", u.getAvatar());
        m.put("campusId", u.getCampusId());
        m.put("campusName", u.getCampusName());
        m.put("creditScore", u.getCreditScore());
        m.put("orgId", u.getDeptId());
        m.put("orgName", u.getCampusName());
        m.put("roleCode", r == null ? null : r.getRoleCode());
        m.put("roleName", r == null ? null : r.getRoleName());
        m.put("dataScope", r == null ? null : r.getDataScope());
        return m;
    }

    public SysRole loadRole(Long userId) {
        SysUserRole ur = userRoleMapper.selectOne(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId).last("limit 1"));
        return ur == null ? null : roleMapper.selectById(ur.getRoleId());
    }

    /** 按角色取菜单并组装树；只返回目录与菜单，按钮权限单独下发到 perm 字段 */
    public List<Map<String, Object>> menuTree(Long roleId) {
        List<Long> menuIds = roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<SysMenu> all = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getVisible, 1)
                .in(SysMenu::getMenuType, List.of("catalog", "menu"))
                .orderByAsc(SysMenu::getSort));
        Map<Long, List<SysMenu>> byParent = all.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));
        return build(0L, byParent);
    }

    private List<Map<String, Object>> build(Long parentId, Map<Long, List<SysMenu>> byParent) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysMenu m : byParent.getOrDefault(parentId, List.of())) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", m.getId());
            node.put("name", m.getMenuName());
            node.put("path", m.getPath());
            node.put("icon", m.getIcon());
            node.put("type", m.getMenuType());
            node.put("perm", m.getPerm());
            List<Map<String, Object>> children = build(m.getId(), byParent);
            if (!children.isEmpty()) {
                node.put("children", children);
            }
            list.add(node);
        }
        return list;
    }

    /** 修改个人资料，头像上传后右上角同步显示 */
    @Transactional
    public void updateProfile(Dtos.ProfileReq req) {
        SysUser u = new SysUser();
        u.setId(UserContext.userId());
        u.setRealName(req.realName());
        u.setPhone(req.phone());
        u.setAvatar(req.avatar());
        userMapper.updateById(u);
        trace.log("个人中心", "修改资料", "sys_user:" + UserContext.userId(), "姓名 " + req.realName());
    }

    @Transactional
    public void updatePassword(Dtos.PasswordReq req) {
        SysUser user = userMapper.selectById(UserContext.userId());
        if (user == null || !encoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BizException(4000, "原密码不正确");
        }
        SysUser u = new SysUser();
        u.setId(user.getId());
        u.setPassword(encoder.encode(req.newPassword()));
        userMapper.updateById(u);
        trace.log("个人中心", "修改密码", "sys_user:" + user.getId(), "密码已更新");
    }
}
