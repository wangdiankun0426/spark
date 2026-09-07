package com.spark.common.bean.base;

import com.spark.common.bean.sys.entity.Session;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.RoleTypeEnum;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/6 21:11
 * 当前线程登录的用户信息
 */
public class SessionHolder {
    /**
     * 用户id
     */
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    /**
     * 用户sessionId
     */
    private static final ThreadLocal<String> currentSessionId = new ThreadLocal<>();

    /**
     * 部门id
     */
    private static final ThreadLocal<Long> currentDeptId = new ThreadLocal<>();

    /**
     * 数据权限
     */
    private static final ThreadLocal<Integer> currentDataScop = new ThreadLocal<>();

    /**
     * 部门ids
     */
    private static final ThreadLocal<String> currentDeptIds = new ThreadLocal<>();

    /**
     * 角色类型位
     */
    private static final ThreadLocal<Integer> currentRoleType = new ThreadLocal<>();

    /**
     * 上下文
     */
    private static final ThreadLocal<BaseContext> context = new ThreadLocal<>();

    public static void setCurrentDeptId(Long userId) {
        currentDeptId.set(userId);
    }

    public static Long getCurrentDeptId() {
        return currentDeptId.get();
    }

    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    public static void setCurrentSessionId(String sessionId) {
        currentSessionId.set(sessionId);
    }

    public static String getCurrentSessionId() {
        return currentSessionId.get();
    }

    public static void setCurrentDataScop(Integer dataScope) {
        currentDataScop.set(dataScope);
    }

    public static Integer getCurrentDataScop() {
        return currentDataScop.get();
    }

    public static void setCurrentDeptIds(String deptIds) {
        currentDeptIds.set(deptIds);
    }

    public static String getCurrentDeptIds() {
        return currentDeptIds.get();
    }

    public static void setCurrentRoleType(Integer roleType) {
        currentRoleType.set(roleType);
    }

    public static Integer getCurrentRoleType() {
        return currentRoleType.get();
    }

    /**
     * 设置上下文
     * @param baseContext
     */
    public static void setContext(BaseContext baseContext) {
        context.set(baseContext);
    }

    /**
     * 获取上下文
     */
    public static BaseContext getContext() {
        return context.get();
    }

    /**
     * 初始化线程session缓存
     */
    public static void initLocalSession(Session session) {
        if(session == null || session.getUserId() == null) {
            throw new BaseException(ErrorCodeEnum.NOT_LOGIN);
        }
        setCurrentSessionId(session.getSessionId());
        setCurrentUserId(session.getUserId());
        setCurrentDeptId(session.getDeptId());
        setCurrentDataScop(session.getDataScope());
        setCurrentDeptIds(session.getDeptIds());
        setCurrentRoleType(session.getRoleType());
    }

    /**
     * 清空当前线程中session缓存
     */
    public static void clearLocalSession(){
        currentUserId.remove();
        currentSessionId.remove();
        currentDeptId.remove();
        currentDataScop.remove();
        currentDeptIds.remove();
        currentRoleType.remove();
        context.remove();
    }

    /**
     * 判读当前用户，当前空间，是否为管理员
     * @return
     */
    public static boolean isAdmin(){
        Integer roleType = getCurrentRoleType();
        if (roleType == null) {
            return false;
        }
        return RoleTypeEnum.hasRole(SessionHolder.getCurrentRoleType(), RoleTypeEnum.SYS_ADMIN)
                || RoleTypeEnum.hasRole(SessionHolder.getCurrentRoleType(), RoleTypeEnum.ORG_ADMIN);
    }
}
