package com.spark.workflow.engine.executor;

import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.WorkflowTemplateTypeEnum;
import com.spark.llm.IAgent;
import com.spark.llm.agent.AgentFactory;
import com.spark.manage.BaseService;
import com.spark.utils.JsonUtil;
import com.spark.utils.MapUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-26 17:00:00
 * Agent任务节点执行器：调用AgentFactory执行智能体任务
 */
@Component
public class AgentTaskExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(AgentTaskExecutor.class);

    @Autowired
    private AgentFactory agentFactory;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return WorkflowTemplateTypeEnum.AGENT_TASK.getValue();
    }

    /**
     * 执行节点
     * @param nodeId 节点id
     * @param config 节点配置
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 节点输出
     */
    @Override
    public Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        Map<String, String> output = new HashMap<>();
        if (config == null) {
            throw new IllegalArgumentException(ErrorCodeEnum.INVALID_PARAM.getDesc());
        }
        Long agentId = MapUtil.getLongVal(config, "agentId");
        if (agentId == null) {
            throw new IllegalArgumentException("Agent节点未配置Agent");
        }
        String taskCode = MapUtil.getStringVal(config, "task");
        String task = super.generateFlowValue(taskCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(task)) {
            throw new IllegalArgumentException("Agent节点任务描述解析失败");
        }
        String memoryIdCode = MapUtil.getStringVal(config, "memoryId");
        String memoryId = null;
        if (StringUtil.isNotBlank(memoryIdCode)) {
            memoryId = super.generateFlowValue(memoryIdCode, null, valueMap, showValueMap, input);
        }
        try {
            IAgent agent = agentFactory.build(agentId);
            if (agent == null) {
                throw new IllegalArgumentException("Agent构建失败: " + agentId);
            }
            String response;
            if (StringUtil.isNotBlank(memoryId)) {
                response = agent.chat(memoryId, task);
            } else {
                response = agent.chat(nodeId, task);
            }
            if (StringUtil.isBlank(response)) {
                response = "";
            }
            output.put(nodeId + "text", response);
            output.put(nodeId + "result", response);
            boolean validJson = JsonUtil.isValidJson(response);
            if (validJson) {
                HashMap<String, Object> map = JsonUtil.toObject(response, HashMap.class);
                if (map != null) {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        output.put(nodeId + key, String.valueOf(value));
                    }
                }
            }
            logger.info("Agent node executed, agentId={}, task length={}", agentId, task.length());
        } catch (Exception e) {
            logger.error("Agent execution failed, agentId={}", agentId, e);
            throw new RuntimeException("Agent执行失败: " + e.getMessage(), e);
        }
        return output;
    }
}
