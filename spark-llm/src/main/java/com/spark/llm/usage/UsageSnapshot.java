package com.spark.llm.usage;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 模型调用瞬间的会话与场景快照
 */
@Data
public class UsageSnapshot {

    /**
     * 租户id
     */
    private final Long tenantId;

    /**
     * 使用人id
     */
    private final Long userId;

    /**
     * 部门id
     */
    private final Long deptId;

    /**
     * 调用场景编码
     */
    private final String scene;
}
