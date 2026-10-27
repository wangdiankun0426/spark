package com.spark.common.utils;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.base.SessionSnapshot;
import com.spark.common.enums.ModelUsageSceneEnum;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-28 10:00:00
 * 登录会话快照的跨线程传播工具，提交线程捕获、执行线程恢复、执行完还原或清空
 */
public class SessionContextUtil {

    /**
     * 无主系统调用兜底租户id
     */
    private static final Long SYSTEM_TENANT_ID = 0L;

    /**
     * 无主系统调用兜底用户id
     */
    private static final Long SYSTEM_USER_ID = 101L;

    /**
     * 无主系统调用兜底部门id
     */
    private static final Long SYSTEM_DEPT_ID = 0L;

    private SessionContextUtil() {
    }

    /**
     * 捕获当前线程会话快照，不带模型调用场景
     * @return 会话快照
     */
    public static SessionSnapshot capture() {
        return capture(null);
    }

    /**
     * 捕获当前线程会话快照并指定模型调用场景
     * @param scene 模型调用场景
     * @return 会话快照
     */
    public static SessionSnapshot capture(ModelUsageSceneEnum scene) {
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        return new SessionSnapshot(
                SessionHolder.getCurrentSessionId(),
                SessionHolder.getCurrentUserId(),
                SessionHolder.getCurrentDeptId(),
                SessionHolder.getCurrentDeptIds(),
                SessionHolder.getCurrentDataScop(),
                SessionHolder.getCurrentRoleType(),
                SessionHolder.getCurrentTenantId(),
                SessionHolder.getAccountType(),
                SessionHolder.getCurrentLoginPlatform(),
                scene,
                mdcMap);
    }

    /**
     * 构造仅含租户、用户、部门的快照
     * @param tenantId 租户id
     * @param userId 用户id
     * @param deptId 部门id
     * @return 会话快照
     */
    public static SessionSnapshot basic(Long tenantId, Long userId, Long deptId) {
        return new SessionSnapshot(null, userId, deptId, null, null, null, tenantId, null, null, null, null);
    }

    /**
     * 无主系统调用快照
     * @return 会话快照
     */
    public static SessionSnapshot system() {
        return basic(SYSTEM_TENANT_ID, SYSTEM_USER_ID, SYSTEM_DEPT_ID);
    }

    /**
     * 在当前线程恢复快照，允许字段为空，执行完必须由clear或再次apply覆盖
     * @param snapshot 会话快照
     */
    public static void apply(SessionSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        SessionHolder.setCurrentSessionId(snapshot.getSessionId());
        SessionHolder.setCurrentUserId(snapshot.getUserId());
        SessionHolder.setCurrentDeptId(snapshot.getDeptId());
        SessionHolder.setCurrentDeptIds(snapshot.getDeptIds());
        SessionHolder.setCurrentDataScop(snapshot.getDataScope());
        SessionHolder.setCurrentRoleType(snapshot.getRoleType());
        SessionHolder.setCurrentTenantId(snapshot.getTenantId());
        SessionHolder.setAccountType(snapshot.getAccountType());
        SessionHolder.setCurrentLoginPlatform(snapshot.getLoginPlatform());
        ModelUsageSceneHolder.clear();
        if (snapshot.getScene() != null) {
            ModelUsageSceneHolder.push(snapshot.getScene());
        }
        if (snapshot.getMdcMap() == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(snapshot.getMdcMap());
        }
    }

    /**
     * 清空当前线程会话与场景、MDC
     */
    public static void clear() {
        SessionHolder.clearLocalSession();
        ModelUsageSceneHolder.clear();
        MDC.clear();
    }

    /**
     * 判断快照是否无登录身份
     * @param snapshot 会话快照
     * @return 无用户id时返回true
     */
    public static boolean isEmpty(SessionSnapshot snapshot) {
        return snapshot == null || snapshot.getUserId() == null;
    }

    /**
     * 携带快照执行任务，执行完还原线程原会话
     * @param snapshot 会话快照
     * @param task 任务
     */
    public static void runWith(SessionSnapshot snapshot, Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空");
        }
        if (isEmpty(snapshot)) {
            task.run();
            return;
        }
        SessionSnapshot previous = capture();
        try {
            apply(snapshot);
            task.run();
        } finally {
            restore(previous);
        }
    }

    /**
     * 包装Runnable，提交线程捕获快照、执行线程恢复
     * @param task 原始任务
     * @return 带会话传播的任务
     */
    public static Runnable wrap(Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空");
        }
        SessionSnapshot snapshot = capture();
        return () -> runWith(snapshot, task);
    }

    /**
     * 包装Callable，提交线程捕获快照、执行线程恢复
     * @param task 原始任务
     * @param <T> 返回类型
     * @return 带会话传播的任务
     */
    public static <T> Callable<T> wrap(Callable<T> task) {
        if (task == null) {
            throw new IllegalArgumentException("任务不能为空");
        }
        SessionSnapshot snapshot = capture();
        return () -> {
            if (isEmpty(snapshot)) {
                return task.call();
            }
            SessionSnapshot previous = capture();
            try {
                apply(snapshot);
                return task.call();
            } finally {
                restore(previous);
            }
        };
    }

    /**
     * 还原执行前线程会话，无原会话时清空
     * @param previous 执行前快照
     */
    private static void restore(SessionSnapshot previous) {
        if (isEmpty(previous)) {
            clear();
        } else {
            apply(previous);
        }
    }
}
