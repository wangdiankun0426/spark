package com.spark.common.bean.base;

import com.spark.common.enums.ModelUsageSceneEnum;
import lombok.Data;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-28 10:00:00
 * 跨线程传递的登录会话快照
 */
@Data
public class SessionSnapshot {

    /**
     * 登录后sessionId
     */
    private final String sessionId;

    /**
     * 用户id
     */
    private final Long userId;

    /**
     * 部门id
     */
    private final Long deptId;

    /**
     * 部门ids
     */
    private final String deptIds;

    /**
     * 数据权限
     */
    private final Integer dataScope;

    /**
     * 角色类型
     */
    private final Integer roleType;

    /**
     * 租户id
     */
    private final Long tenantId;

    /**
     * 账号类型
     */
    private final Integer accountType;

    /**
     * 登录平台
     */
    private final Integer loginPlatform;

    /**
     * 模型调用场景，为空表示不切换场景
     */
    private final ModelUsageSceneEnum scene;

    /**
     * 日志MDC上下文
     */
    private final Map<String, String> mdcMap;
}
