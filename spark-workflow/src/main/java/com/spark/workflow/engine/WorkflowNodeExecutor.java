package com.spark.workflow.engine;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 15:00:00
 * 节点执行器接口，每种节点类型需实现
 */
public interface WorkflowNodeExecutor {

    /**
     * 支持的节点类型
     * @return
     */
    String getNodeType();

    /**
     * 执行节点
     * @param nodeConfig 节点配置（DAG JSON中的config对象）
     * @param input 当前节点输入
     * @param context 全局变量上下文（可读写）
     * @return 节点输出，写入context
     */
    Map<String, Object> execute(Map<String, Object> nodeConfig, Map<String, Object> input, Map<String, Object> context);
}
