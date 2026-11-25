package com.grad.secondhand.common;

/**
 * 当前登录用户上下文，由 JWT 过滤器写入，Service 只读。
 * 权限按需求文档第 2 节「菜单权限 + 按钮权限 + 数据范围」三层控制：
 * all 全部、dept 本部门、self 本人。前端隐藏按钮不等于授权，写接口必须后端复核。
 */
public class UserContext {

    public record CurrentUser(Long userId, String username, String realName, String roleCode,
                              Long orgId, String orgName, String dataScope) {
    }

    /** 角色编码，与 sys_role.role_code 一致 */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";       // 平台管理员
    public static final String ROLE_OPERATOR = "ROLE_OPERATOR"; // 校园运营人员
    public static final String ROLE_SELLER = "ROLE_SELLER";     // 学生卖家
    public static final String ROLE_BUYER = "ROLE_BUYER";       // 学生买家

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.userId();
    }

    public static String username() {
        CurrentUser u = HOLDER.get();
        return u == null ? "" : u.username();
    }

    public static String realName() {
        CurrentUser u = HOLDER.get();
        return u == null ? "" : u.realName();
    }

    public static String roleCode() {
        CurrentUser u = HOLDER.get();
        return u == null ? "" : u.roleCode();
    }

    public static Long orgId() {
        CurrentUser u = HOLDER.get();
        return u == null ? null : u.orgId();
    }

    public static String campusName() {
        CurrentUser u = HOLDER.get();
        return u == null ? "" : u.orgName();
    }

    public static String dataScope() {
        CurrentUser u = HOLDER.get();
        return u == null ? "self" : u.dataScope();
    }

    /** 平台管理员：全域数据、系统管理与参数配置 */
    public static boolean isAdmin() {
        return ROLE_ADMIN.equals(roleCode());
    }

    /** 校园运营人员：商品审核、举报受理、信用调整、规则配置 */
    public static boolean isOperator() {
        return ROLE_OPERATOR.equals(roleCode()) || isAdmin();
    }

    /** 学生卖家 */
    public static boolean isSeller() {
        return ROLE_SELLER.equals(roleCode());
    }

    /** 学生买家 */
    public static boolean isBuyer() {
        return ROLE_BUYER.equals(roleCode());
    }

    /** 学生（门户侧），卖家与买家都算 */
    public static boolean isStudent() {
        return isSeller() || isBuyer();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
