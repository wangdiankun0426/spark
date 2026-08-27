package com.spark.workflow.engine.executor;

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
public interface IWfNodeExecutor {

    /**
     * 节点类型
     * @return
     */
    String getNodeType();

    /**
     * 执行节点
     * @param nodeId 节点id
     * @param config 节点配置
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 节点输出
     */
    Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap , Map<String, String> showValueMap);
}
