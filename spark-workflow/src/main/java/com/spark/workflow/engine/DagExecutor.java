package com.spark.workflow.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.spark.bean.workflow.entity.WfInstance;
import com.spark.bean.workflow.entity.WfInstanceNode;
import com.spark.bean.workflow.query.WfInstanceQuery;
import com.spark.bean.workflow.result.WfInstanceResult;
import com.spark.dao.workflow.WfInstanceDao;
import com.spark.dao.workflow.WfInstanceNodeDao;
import com.spark.enums.WorkflowInstanceStatusEnum;
import com.spark.enums.WorkflowNodeStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 15:00:00
 * DAG拓扑执行引擎
 */
@Component
public class DagExecutor {
    private static final Logger logger = LoggerFactory.getLogger(DagExecutor.class);
    @Autowired
    private WfInstanceNodeDao instanceNodeDao;
    @Autowired
    private WfInstanceDao instanceDao;
    @Autowired
    private List<WorkflowNodeExecutor> executors;

    /**
     * 异步执行DAG
     */
    @Async("workflowExecutor")
    public void executeAsync(Long instanceId, String dagJson, Map<String, Object> inputParams) {
        execute(instanceId, dagJson, inputParams);
    }

    /**
     * 同步执行DAG
     * @param instanceId
     * @param dagJson
     * @param inputParams
     * @return
     */
    public Map<String, Object> execute(Long instanceId, String dagJson, Map<String, Object> inputParams) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> variables = new HashMap<>();
        if (inputParams != null) {
            variables.putAll(inputParams);
        }
        // 从实例中获取审计信息（异步线程无Session）
        WfInstanceQuery wfInstanceQuery = new WfInstanceQuery();
        wfInstanceQuery.setId(instanceId);
        WfInstanceResult instanceResult = instanceDao.queryInstance(wfInstanceQuery);
        Long instanceCreatedBy = instanceResult != null ? instanceResult.getCreatedBy() : null;

        JSONObject dag = JSON.parseObject(dagJson);
        JSONArray nodesArr = dag.getJSONArray("nodes");
        JSONArray sequencesArr = dag.getJSONArray("sequences");
        if (sequencesArr == null) { sequencesArr = new JSONArray(); }

        // 构建节点索引
        Map<String, JSONObject> nodeMap = new LinkedHashMap<>();
        for (int i = 0; i < nodesArr.size(); i++) {
            JSONObject n = nodesArr.getJSONObject(i);
            nodeMap.put(n.getString("id"), n);
        }

        // 构建邻接表和入度
        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        for (String nid : nodeMap.keySet()) {
            adjacency.put(nid, new ArrayList<>());
            inDegree.put(nid, 0);
        }
        for (int i = 0; i < sequencesArr.size(); i++) {
            JSONObject edge = sequencesArr.getJSONObject(i);
            String src = edge.getString("source");
            String tgt = edge.getString("target");
            if (nodeMap.containsKey(src) && nodeMap.containsKey(tgt)) {
                adjacency.get(src).add(tgt);
                inDegree.put(tgt, inDegree.get(tgt) + 1);
            }
        }

        // 但condition/switch节点的目标节点不应计入入度（它们是条件触发的）
        // 这里简化处理：所有边都计入

        // 拓扑排序 → 分层执行
        Queue<String> queue = new LinkedList<>();
        Map<String, Integer> remainingInDegree = new HashMap<>(inDegree);
        for (Map.Entry<String, Integer> entry : remainingInDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        // 记录已执行的节点
        Set<String> executed = new HashSet<>();
        // 记录节点输出到context
        Map<String, Map<String, Object>> nodeOutputs = new HashMap<>();

        try {
            while (!queue.isEmpty()) {
                String nodeId = queue.poll();
                JSONObject node = nodeMap.get(nodeId);
                if (node == null) {
                    continue;
                }
                String nodeType = node.getString("type");
                String nodeName = node.getString("name");
                JSONObject config = node.getJSONObject("config");
                if (config == null) {
                    config = new JSONObject();
                }
                // 收集输入：从所有前置节点的输出中获取
                Map<String, Object> nodeInput = new HashMap<>();
                for (int i = 0; i < sequencesArr.size(); i++) {
                    JSONObject edge = sequencesArr.getJSONObject(i);
                    if (nodeId.equals(edge.getString("target"))) {
                        String prevNodeId = edge.getString("source");
                        if (nodeOutputs.containsKey(prevNodeId)) {
                            nodeInput.putAll(nodeOutputs.get(prevNodeId));
                        }
                    }
                }
                // 创建节点运行记录
                Long runNodeId = insertInstanceNode(instanceId, nodeId, nodeName, nodeType, WorkflowNodeStatusEnum.RUNNING.getValue(), JSON.toJSONString(nodeInput), instanceCreatedBy);
                // 执行节点
                long nodeStart = System.currentTimeMillis();
                Map<String, Object> output;
                try {
                    WorkflowNodeExecutor executor = findExecutor(nodeType);
                    if (executor != null) {
                        output = executor.execute(config, nodeInput, variables);
                    } else {
                        output = new HashMap<>();
                        logger.warn("No executor found for node type: {}", nodeType);
                    }
                    if (output == null) {
                        output = new HashMap<>();
                    }

                    // 对于condition节点，output中包含branch名，用于决定后续执行哪些节点
                    // 对于end节点，将output写入variables作为最终结果

                    long nodeDuration = System.currentTimeMillis() - nodeStart;
                    updateInstanceNode(runNodeId, WorkflowNodeStatusEnum.SUCCESS.getValue(), JSON.toJSONString(output), null, nodeDuration);
                    nodeOutputs.put(nodeId, output);
                    // 将节点输出以节点ID为key挂到共享context，支持 end 节点按 nodeId.output.field 路径取值
                    if (!"start".equals(nodeType) && !"end".equals(nodeType)) {
                        variables.put(nodeId, output);
                    }

                    // condition/switch节点的输出影响后续节点执行
                    if ("condition".equals(nodeType) && output.containsKey("branch")) {
                        String branchName = output.get("branch").toString();
                        // 仅将匹配的分支目标节点加入下一层
                        for (int i = 0; i < sequencesArr.size(); i++) {
                            JSONObject edge = sequencesArr.getJSONObject(i);
                            if (nodeId.equals(edge.getString("source"))) {
                                String edgeCondition = edge.getString("condition");
                                // 匹配condition name或else
                                if (branchName.equals(edgeCondition) || "else".equals(edgeCondition)) {
                                    String tgt = edge.getString("target");
                                    remainingInDegree.put(tgt, remainingInDegree.get(tgt) - 1);
                                    if (remainingInDegree.get(tgt) == 0 && !executed.contains(tgt)) {
                                        queue.add(tgt);
                                    }
                                }
                            }
                        }
                        executed.add(nodeId);
                        continue;
                    }

                } catch (Exception e) {
                    logger.error("Node execution failed: {} (type={})", nodeName, nodeType, e);
                    long nodeDuration = System.currentTimeMillis() - nodeStart;
                    updateInstanceNode(runNodeId, WorkflowNodeStatusEnum.FAILED.getValue(), null, e.getMessage(), nodeDuration);
                    // 失败处理：终止整个工作流
                    updateInstance(instanceId, WorkflowInstanceStatusEnum.FAILED.getValue(), null, "节点[" + nodeName + "]执行失败: " + e.getMessage(), System.currentTimeMillis() - startTime);
                    return variables;
                }
                // 减少后继节点入度
                for (String neighbor : adjacency.get(nodeId)) {
                    remainingInDegree.put(neighbor, remainingInDegree.get(neighbor) - 1);
                    if (remainingInDegree.get(neighbor) == 0 && !executed.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
                executed.add(nodeId);
            }

            // 收集end节点的输出作为最终结果
            Map<String, Object> finalOutput = new HashMap<>();
            for (JSONObject node : nodeMap.values()) {
                if ("end".equals(node.getString("type"))) {
                    if (nodeOutputs.containsKey(node.getString("id"))) {
                        finalOutput.putAll(nodeOutputs.get(node.getString("id")));
                    }
                }
            }
            long totalDuration = System.currentTimeMillis() - startTime;
            updateInstance(instanceId, WorkflowInstanceStatusEnum.SUCCESS.getValue(), JSON.toJSONString(finalOutput), null, totalDuration);
            return finalOutput;
        } catch (Exception e) {
            logger.error("DAG execution failed: instanceId={}", instanceId, e);
            updateInstance(instanceId, WorkflowInstanceStatusEnum.FAILED.getValue(), null, e.getMessage(), System.currentTimeMillis() - startTime);
            return variables;
        }
    }

    /**
     *
     * @param nodeType
     * @return
     */
    private WorkflowNodeExecutor findExecutor(String nodeType) {
        if (executors == null) {
            return null;
        }
        for (WorkflowNodeExecutor e : executors) {
            if (e.getNodeType().equals(nodeType)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 创建实例节点
     * @param instanceId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param status
     * @param inputJson
     * @param createdBy
     * @return
     */
    private Long insertInstanceNode(Long instanceId, String nodeId, String nodeName, String nodeType, int status, String inputJson, Long createdBy) {
        WfInstanceNode rn = new WfInstanceNode();
        rn.setInstanceId(instanceId);
        rn.setNodeId(nodeId);
        rn.setNodeName(nodeName);
        rn.setNodeType(nodeType);
        rn.setStatus(status);
        rn.setInputJson(inputJson);
        rn.setStartedDt(new Timestamp(System.currentTimeMillis()));
        rn.setRetryCount(0);
        if (createdBy != null) {
            rn.setCreatedBy(createdBy);
        }
        instanceNodeDao.insertDB(rn);
        return rn.getId();
    }

    /**
     * 更新实例节点
     * @param nodeId
     * @param status
     * @param outputJson
     * @param errorMsg
     * @param durationMs
     */
    private void updateInstanceNode(Long nodeId, int status, String outputJson, String errorMsg, long durationMs) {
        WfInstanceNode rn = new WfInstanceNode();
        rn.setId(nodeId);
        rn.setStatus(status);
        rn.setOutputJson(outputJson);
        rn.setErrorMsg(errorMsg);
        rn.setDurationMs(durationMs);
        rn.setFinishedDt(new Timestamp(System.currentTimeMillis()));
        instanceNodeDao.updateDBById(rn);
    }

    /**
     * 更新实例
     * @param instanceId
     * @param status
     * @param outputJson
     * @param errorMsg
     * @param durationMs
     */
    private void updateInstance(Long instanceId, int status, String outputJson, String errorMsg, long durationMs) {
        WfInstance instance = new WfInstance();
        instance.setId(instanceId);
        instance.setStatus(status);
        if (outputJson != null) {
            instance.setOutputJson(outputJson);
        }
        if (errorMsg != null) {
            instance.setErrorMsg(errorMsg);
        }
        instance.setDurationMs(durationMs);
        instance.setFinishedDt(new Timestamp(System.currentTimeMillis()));
        instanceDao.updateDBById(instance);
    }
}
