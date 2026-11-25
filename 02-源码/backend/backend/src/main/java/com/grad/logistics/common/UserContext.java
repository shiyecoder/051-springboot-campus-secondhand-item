package com.grad.logistics.common;

/**
 * 当前登录用户上下文。由 JWT 过滤器写入，Service 只读。
 * 数据范围按需求文档「权限与消息规则」三级控制：all 全域、dept 本部门、self 本人、org 本单位。
 */
public class UserContext {

    public record CurrentUser(Long userId, String username, String realName, String roleCode,
                              Long orgId, String orgName, String dataScope) {
    }

    /** 角色编码，与 sys_role.role_code 一致 */
    public static final String ROLE_MANAGER = "ROLE_MANAGER";       // 车队经理/管理员
    public static final String ROLE_DISPATCHER = "ROLE_DISPATCHER"; // 调度员
    public static final String ROLE_DRIVER = "ROLE_DRIVER";         // 驾驶员
    public static final String ROLE_FINANCE = "ROLE_FINANCE";       // 客服/财务
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";     // 客户/货主

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

    public static String orgName() {
        CurrentUser u = HOLDER.get();
        return u == null ? "" : u.orgName();
    }

    public static String dataScope() {
        CurrentUser u = HOLDER.get();
        return u == null ? "self" : u.dataScope();
    }

    /** 车队经理/管理员：全域数据、审批与配置 */
    public static boolean isManager() {
        return ROLE_MANAGER.equals(roleCode());
    }

    /** 调度员：审核需求、建单、配车、调整计划 */
    public static boolean isDispatcher() {
        return ROLE_DISPATCHER.equals(roleCode()) || isManager();
    }

    /** 驾驶员：仅本人任务，可上报节点、费用、异常与签收凭证 */
    public static boolean isDriver() {
        return ROLE_DRIVER.equals(roleCode());
    }

    /** 客服/财务：异常跟进、费用审核、收付款与对账；不能修改运输节点 */
    public static boolean isFinance() {
        return ROLE_FINANCE.equals(roleCode()) || isManager();
    }

    /** 客户/货主：仅本单位运单 */
    public static boolean isCustomer() {
        return ROLE_CUSTOMER.equals(roleCode());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
