package com.spark.workflow.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.spark.bean.base.BaseContext;
import com.spark.bean.base.SessionHolder;
import com.spark.bean.system.entity.Session;
import com.spark.bean.workflow.config.RetryConfig;
import com.spark.bean.workflow.entity.WfInstance;
import com.spark.bean.workflow.entity.WfInstanceNode;
import com.spark.bean.workflow.exception.HumanReviewRequiredException;
import com.spark.dao.workflow.WfInstanceDao;
import com.spark.dao.workflow.WfInstanceNodeDao;
import com.spark.enums.WorkflowTemplateTypeEnum;
import com.spark.enums.WorkflowInstanceStatusEnum;
import com.spark.enums.WorkflowNodeStatusEnum;
import com.spark.utils.StringUtil;
import com.spark.workflow.engine.executor.IWfNodeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeoutException;

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
    private List<IWfNodeExecutor> executors;
    @Autowired
    private RetryStrategy retryStrategy;
    @Autowired
    private TimeoutStrategy timeoutStrategy;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 异步执行DAG
     *
     * @param session
     * @param context
     * @param dagJson
     */
    @Async("workflowExecutor")
    public void executeAsync(Session session, BaseContext context, String dagJson) {
        try {
            SessionHolder.setCurrentUserId(session.getUserId());
            execute(context, dagJson);
        } catch (Exception e) {
            logger.error("executeAsync error", e);
        } finally {
            SessionHolder.clearLocalSession();
        }
    }

    /**
     * 同步执行DAG
     *
     * @param context
     * @param dagJson
     * @return
     */
    public void execute(BaseContext context, String dagJson) {
        if (StringUtil.isBlank(dagJson)) {
            logger.error("dagJson is null");
            return;
        }
        WfInstance wfInstance = context.getVal(WfInstance.class);
        if (wfInstance == null) {
            logger.error("wfInstance is null");
            return;
        }
        Map<String, String> valueMap = (Map<String, String>) context.getOrDefault("valueMap", new HashMap<>());
        Map<String, String> showValueMap = (Map<String, String>) context.getOrDefault("showValueMap", new HashMap<>());
        Long instanceId = wfInstance.getId();
        long startTime = System.currentTimeMillis();
        // 创建变量空间
        WorkflowVariableSpace variableSpace = new WorkflowVariableSpace(null, valueMap, showValueMap);
        JSONObject dag = JSON.parseObject(dagJson);
        JSONArray nodes = dag.getJSONArray("nodes");
        JSONArray sequences = dag.getJSONArray("sequences");
        if (sequences == null) {
            sequences = new JSONArray();
        }
        // 构建节点
        Map<String, JSONObject> nodeMap = new LinkedHashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            nodeMap.put(node.getString("id"), node);
        }
        // 构建邻接表和入度
        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        for (String nid : nodeMap.keySet()) {
            adjacency.put(nid, new ArrayList<>());
            inDegree.put(nid, 0);
        }
        for (int i = 0; i < sequences.size(); i++) {
            JSONObject sequence = sequences.getJSONObject(i);
            String src = sequence.getString("source");
            String tgt = sequence.getString("target");
            if (nodeMap.containsKey(src) && nodeMap.containsKey(tgt)) {
                adjacency.get(src).add(tgt);
                inDegree.put(tgt, inDegree.get(tgt) + 1);
            }
        }
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
        // 记录被跳过的节点
        Set<String> skipped = new HashSet<>();
        // 记录收到过"活跃路径"放行的节点
        Set<String> liveMarked = new HashSet<>();
        Map<String, IWfNodeExecutor> executorMap = new HashMap<>();
        for (IWfNodeExecutor executor : executors) {
            executorMap.put(executor.getNodeType(), executor);
        }
        try {
            while (!queue.isEmpty()) {
                String nodeId = queue.poll();
                if (skipped.contains(nodeId)) {
                    continue;
                }

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
                // 节点输入，从变量空间获取前置节点输出
                Map<String, String> nodeInput = new HashMap<>();
                for (int i = 0; i < sequences.size(); i++) {
                    JSONObject sequence = sequences.getJSONObject(i);
                    if (nodeId.equals(sequence.getString("target"))) {
                        String prevNodeId = sequence.getString("source");
                        Map<String, String> prevOutput = variableSpace.getNodeOutput(prevNodeId);
                        if (prevOutput != null) {
                            nodeInput.putAll(prevOutput);
                        }
                    }
                }
                // 创建节点运行记录
                Long runNodeId = this.insertInstanceNode(instanceId, nodeId, nodeName, nodeType, WorkflowNodeStatusEnum.RUNNING.getValue(), JSON.toJSONString(nodeInput));
                long nodeStart = System.currentTimeMillis();
                // 节点输出（带超时和重试）
                Map<String, String> nodeOutput = new HashMap<>();
                try {
                    IWfNodeExecutor executor = executorMap.get(nodeType);
                    if (executor != null) {
                        nodeOutput = this.executeWithTimeoutRetry(nodeId, nodeName, executor, config, nodeInput, valueMap, showValueMap);
                    } else {
                        // 网关/起止等无执行器节点：输入直传输出，保证变量跨节点传递
                        nodeOutput = new HashMap<>(nodeInput);
                    }
                    if (nodeOutput == null) {
                        nodeOutput = new HashMap<>();
                    }
                    // 更新变量空间
                    variableSpace.setNodeOutput(nodeId, nodeOutput);
                    long nodeDuration = System.currentTimeMillis() - nodeStart;
                    this.updateInstanceNode(runNodeId, WorkflowNodeStatusEnum.SUCCESS.getValue(), JSON.toJSONString(nodeOutput), null, nodeDuration);
                    // 排他网关：按出边配置的走向条件求值，确定生效分支放行，无命中抛异常
                    if (WorkflowTemplateTypeEnum.EXCLUSIVE_GATEWAY.getValue().equals(nodeType)) {
                        this.routeExclusiveGateway(nodeId, sequences, nodeInput, valueMap, showValueMap, instanceId, nodeMap, remainingInDegree, executed, skipped, liveMarked, queue);
                        executed.add(nodeId);
                        continue;
                    }
                    // 并行分支网关：将所有出边节点加入队列并行执行
                    if (WorkflowTemplateTypeEnum.PARALLEL_GATEWAY.getValue().equals(nodeType)) {
                        List<String> outgoing = this.getOutgoingNodes(nodeId, sequences);
                        if (outgoing.size() > 1) {
                            // 分支网关：将所有出边节点加入队列
                            logger.info("Parallel fork: nodeId={}, branches={}", nodeId, outgoing);
                            queue.addAll(outgoing);
                            executed.add(nodeId);
                            continue;
                        }
                        // 合并网关：检查是否所有入边分支都已完成
                        List<String> incoming = this.getIncomingNodes(nodeId, sequences);
                        if (incoming.size() > 1) {
                            boolean allCompleted = true;
                            for (String prevNodeId : incoming) {
                                if (!executed.contains(prevNodeId)) {
                                    allCompleted = false;
                                    break;
                                }
                            }
                            if (!allCompleted) {
                                // 还有分支未完成，跳过本次执行，等待后续轮次
                                logger.info("Parallel join waiting: nodeId={}, completed={}", nodeId, executed);
                                // 重新加入队列
                                queue.add(nodeId);
                                continue;
                            }
                            logger.info("Parallel join completed: nodeId={}", nodeId);
                        }
                    }
                } catch (HumanReviewRequiredException e) {
                    // 人工审核节点需要暂停
                    logger.info("Human review required: nodeId={}, prompt={}", e.getNodeId(), e.getPrompt());
                    long nodeDuration = System.currentTimeMillis() - nodeStart;
                    // 更新节点状态为等待审核
                    Map<String, String> reviewInfo = new HashMap<>();
                    reviewInfo.put("prompt", e.getPrompt());
                    reviewInfo.put("reviewerIds", e.getReviewerIds() != null ? e.getReviewerIds() : "");
                    reviewInfo.put("reviewType", e.getReviewType());
                    reviewInfo.put("requireComment", String.valueOf(e.isRequireComment()));
                    this.updateInstanceNode(runNodeId, WorkflowNodeStatusEnum.WAITING.getValue(), JSON.toJSONString(reviewInfo), null, nodeDuration);
                    // 暂停实例，等待人工审核
                    this.updateInstance(instanceId, WorkflowInstanceStatusEnum.PAUSED.getValue(), null, System.currentTimeMillis() - startTime);
                    return;
                } catch (Exception e) {
                    logger.error("Node execution failed={} (type={})", nodeName, nodeType, e);
                    long nodeDuration = System.currentTimeMillis() - nodeStart;
                    this.updateInstanceNode(runNodeId, WorkflowNodeStatusEnum.FAILED.getValue(), null, e.getMessage(), nodeDuration);
                    this.updateInstance(instanceId, WorkflowInstanceStatusEnum.FAILED.getValue(), "节点[" + nodeName + "]执行失败: " + e.getMessage(), System.currentTimeMillis() - startTime);
                    return;
                }
                // 减少后继节点入度
                for (String neighbor : adjacency.get(nodeId)) {
                    this.releaseNode(neighbor, true, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue);
                }
                executed.add(nodeId);
            }
            long totalDuration = System.currentTimeMillis() - startTime;
            this.updateInstance(instanceId, WorkflowInstanceStatusEnum.SUCCESS.getValue(), null, totalDuration);
        } catch (Exception e) {
            logger.error("DAG execution failed instanceId={}", instanceId, e);
            long totalDuration = System.currentTimeMillis() - startTime;
            this.updateInstance(instanceId, WorkflowInstanceStatusEnum.FAILED.getValue(), e.getMessage(), totalDuration);
        }
    }

    /**
     * 排他网关路由
     * @param nodeId 网关节点ID
     * @param sequences 边数组
     * @param nodeInput 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @param instanceId 实例ID
     * @param nodeMap 节点索引
     * @param remainingInDegree 剩余入度
     * @param executed 已执行集合
     * @param skipped 已跳过集合
     * @param liveMarked 收到过活跃路径放行的节点集合
     * @param queue 执行队列
     */
    private void routeExclusiveGateway(String nodeId, JSONArray sequences, Map<String, String> nodeInput,
                                       Map<String, String> valueMap, Map<String, String> showValueMap, Long instanceId,
                                       Map<String, JSONObject> nodeMap, Map<String, Integer> remainingInDegree,
                                       Set<String> executed, Set<String> skipped, Set<String> liveMarked, Queue<String> queue) {
        List<JSONObject> outgoing = new ArrayList<>();
        for (int i = 0; i < sequences.size(); i++) {
            JSONObject sequence = sequences.getJSONObject(i);
            if (nodeId.equals(sequence.getString("source"))) {
                outgoing.add(sequence);
            }
        }
        // 按配置顺序求值各出边 el 表达式走向条件，取第一个命中
        String activeTarget = null;
        for (JSONObject sequence : outgoing) {
            String conditionExpression = sequence.getString("conditionExpression");
            if (StringUtil.isNotBlank(conditionExpression) && this.matchExpression(conditionExpression, nodeInput, valueMap, showValueMap)) {
                activeTarget = sequence.getString("target");
                break;
            }
        }
        // 全部不命中
        if (activeTarget == null) {
            throw new IllegalArgumentException("条件分支节点未匹配到任何走向条件");
        }
        // 命中边活跃放行，其余出边按死路处理
        for (JSONObject sequence : outgoing) {
            boolean live = sequence.getString("target").equals(activeTarget);
            this.releaseNode(sequence.getString("target"), live, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue);
        }
    }

    /**
     * 释放目标节点入度；归零后按放行来源决定入队执行或标记跳过
     * @param targetId 目标节点ID
     * @param live 是否来自活跃路径（条件分支未匹配的边视为死路）
     * @param instanceId 实例ID
     * @param nodeMap 节点索引
     * @param sequences 边数组
     * @param remainingInDegree 剩余入度
     * @param executed 已执行集合
     * @param skipped 已跳过集合
     * @param liveMarked 收到过活跃路径放行的节点集合
     * @param queue 执行队列
     */
    private void releaseNode(String targetId, boolean live, Long instanceId,
                             Map<String, JSONObject> nodeMap, JSONArray sequences,
                             Map<String, Integer> remainingInDegree, Set<String> executed,
                             Set<String> skipped, Set<String> liveMarked, Queue<String> queue) {
        Integer degree = remainingInDegree.get(targetId);
        if (degree == null) {
            return;
        }
        if (live) {
            liveMarked.add(targetId);
        }
        int newDegree = degree - 1;
        remainingInDegree.put(targetId, newDegree);
        if (newDegree != 0 || executed.contains(targetId) || skipped.contains(targetId)) {
            return;
        }
        if (liveMarked.contains(targetId)) {
            queue.add(targetId);
        } else {
            this.skipNode(targetId, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue);
        }
    }

    /**
     * 标记节点为跳过，并向其后继传递死路消除
     * @param targetId 目标节点ID
     * @param instanceId 实例ID
     * @param nodeMap 节点索引
     * @param sequences 边数组
     * @param remainingInDegree 剩余入度
     * @param executed 已执行集合
     * @param skipped 已跳过集合
     * @param liveMarked 收到过活跃路径放行的节点集合
     * @param queue 执行队列
     */
    private void skipNode(String targetId, Long instanceId,
                          Map<String, JSONObject> nodeMap, JSONArray sequences,
                          Map<String, Integer> remainingInDegree, Set<String> executed,
                          Set<String> skipped, Set<String> liveMarked, Queue<String> queue) {
        skipped.add(targetId);
        JSONObject node = nodeMap.get(targetId);
        String nodeName = node != null ? node.getString("name") : targetId;
        String nodeType = node != null ? node.getString("type") : "";
        // 写一条跳过状态的节点记录，供运行日志展示
        this.insertInstanceNode(instanceId, targetId, nodeName, nodeType, WorkflowNodeStatusEnum.SKIPPED.getValue(), null);
        // 向后继传递死路：后继入度归零且无活跃路径到达时同样跳过
        for (int i = 0; i < sequences.size(); i++) {
            JSONObject sequence = sequences.getJSONObject(i);
            if (targetId.equals(sequence.getString("source"))) {
                this.releaseNode(sequence.getString("target"), false, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue);
            }
        }
    }

    /**
     * 获取节点的所有出边目标节点
     * @param nodeId 节点ID
     * @param sequences 边数组
     * @return 出边目标节点ID列表
     */
    private List<String> getOutgoingNodes(String nodeId, JSONArray sequences) {
        List<String> outgoing = new ArrayList<>();
        for (int i = 0; i < sequences.size(); i++) {
            JSONObject sequence = sequences.getJSONObject(i);
            if (nodeId.equals(sequence.getString("source"))) {
                outgoing.add(sequence.getString("target"));
            }
        }
        return outgoing;
    }

    /**
     * 获取节点的所有入边源节点
     * @param nodeId 节点ID
     * @param sequences 边数组
     * @return 入边源节点ID列表
     */
    private List<String> getIncomingNodes(String nodeId, JSONArray sequences) {
        List<String> incoming = new ArrayList<>();
        for (int i = 0; i < sequences.size(); i++) {
            JSONObject sequence = sequences.getJSONObject(i);
            if (nodeId.equals(sequence.getString("target"))) {
                incoming.add(sequence.getString("source"));
            }
        }
        return incoming;
    }

    /**
     * 以超时+重试策略执行节点：超时控制在内层，重试在外层
     * 超时与重试参数从节点 config 读取，未配置时使用默认值
     * @param nodeId 节点ID
     * @param nodeName 节点名称
     * @param executor 节点执行器
     * @param config 节点配置
     * @param nodeInput 节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 节点输出
     */
    private Map<String, String> executeWithTimeoutRetry(String nodeId, String nodeName, IWfNodeExecutor executor,
                                                         JSONObject config, Map<String, String> nodeInput,
                                                         Map<String, String> valueMap, Map<String, String> showValueMap) {
        Long timeoutMs = config.getLong("timeoutMs");
        if (timeoutMs == null || timeoutMs <= 0) {
            timeoutMs = TimeoutStrategy.DEFAULT_TIMEOUT_MS;
        }

        // 重试配置：未配置的字段使用 RetryConfig 默认值
        RetryConfig retryConfig = new RetryConfig();
        if (config.containsKey("maxRetries")) {
            retryConfig.setMaxRetries(config.getIntValue("maxRetries"));
        }
        if (config.containsKey("retryInitialInterval")) {
            retryConfig.setInitialInterval(config.getLong("retryInitialInterval"));
        }
        if (config.containsKey("retryMultiplier")) {
            retryConfig.setMultiplier(config.getDouble("retryMultiplier"));
        }
        if (config.containsKey("retryMaxInterval")) {
            retryConfig.setMaxInterval(config.getLong("retryMaxInterval"));
        }

        final Long finalTimeoutMs = timeoutMs;
        return retryStrategy.executeWithRetry(() -> {
            try {
                return timeoutStrategy.executeWithTimeout(
                        () -> executor.execute(nodeId, config, nodeInput, valueMap, showValueMap),
                        finalTimeoutMs);
            } catch (TimeoutException e) {
                throw new RuntimeException("节点[" + nodeName + "]执行超时，超过" + finalTimeoutMs + "ms", e);
            } catch (Exception e) {
                throw new RuntimeException("节点[" + nodeName + "]执行失败: " + e.getMessage(), e);
            }
        }, retryConfig);
    }

    /**
     * 创建实例节点
     * @param instanceId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param status
     * @param inputJson
     * @return
     */
    private Long insertInstanceNode(Long instanceId, String nodeId, String nodeName, String nodeType, int status, String inputJson) {
        WfInstanceNode rn = new WfInstanceNode();
        rn.setInstanceId(instanceId);
        rn.setNodeId(nodeId);
        rn.setNodeName(nodeName);
        rn.setNodeType(nodeType);
        rn.setStatus(status);
        rn.setInputJson(inputJson);
        rn.setStartedDt(new Timestamp(System.currentTimeMillis()));
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
     * @param errorMsg
     * @param durationMs
     */
    private void updateInstance(Long instanceId, int status, String errorMsg, long durationMs) {
        WfInstance instance = new WfInstance();
        instance.setId(instanceId);
        instance.setStatus(status);
        if (errorMsg != null) {
            instance.setErrorMsg(errorMsg);
        }
        instance.setDurationMs(durationMs);
        instance.setFinishedDt(new Timestamp(System.currentTimeMillis()));
        instanceDao.updateDBById(instance);
    }

    /**
     * 求值一条出边上的 el 表达式（SpEL）是否满足，如 ${amount > 80 && status == '1'}
     * @param expression el 表达式
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 表达式求值为 true 时返回 true；表达式为空或求值异常时返回 false
     */
    public boolean matchExpression(String expression, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        if (StringUtil.isBlank(expression)) {
            return false;
        }
        String expr = expression.trim();
        if (expr.startsWith("${") && expr.endsWith("}")) {
            expr = expr.substring(2, expr.length() - 1);
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.addPropertyAccessor(new MapPropertyAccessor());
            Map<String, Object> objectMap = this.buildVariableMap(input, valueMap, showValueMap);
            context.setRootObject(objectMap);
            Expression parsed = parser.parseExpression(expr);
            return Boolean.TRUE.equals(parsed.getValue(context, Boolean.class));
        } catch (Exception e) {
            logger.warn("exclusive gateway condition evaluate failed={}, error={}", expression, e.getMessage());
            return false;
        }
    }

    /**
     * 合并求值变量
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 变量集合
     */
    private Map<String, Object> buildVariableMap(Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        Map<String, Object> vars = new HashMap<>();
        vars.putAll(this.convertValueMap(input));
        vars.putAll(this.convertValueMap(showValueMap));
        vars.putAll(this.convertValueMap(valueMap));
        return vars;
    }

    /**
     * 变量值转数值类型用于数值比较；非纯数字字符串保持原样（字符串比较）
     * @param src 原始变量集合
     * @return 转换后的变量集合
     */
    private Map<String, Object> convertValueMap(Map<String, String> src) {
        Map<String, Object> out = new HashMap<>();
        if (src == null) {
            return out;
        }
        for (Map.Entry<String, String> entry : src.entrySet()) {
            out.put(entry.getKey(), this.parseNumberValue(entry.getValue()));
        }
        return out;
    }

    /**
     * 将字符串解析为数值类型，解析失败返回原字符串
     * @param value 变量值
     * @return 数值或原字符串
     */
    private Object parseNumberValue(String value) {
        if (StringUtil.isBlank(value)) {
            return value;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * 支持 SpEL 以裸属性名读取 Map 变量：默认 accessor 无法直接读 HashMap 的 key，
     * 注册后 ${n_xxxcompliant == 'true'} 这类表达式可正常求值
     */
    private static class MapPropertyAccessor implements PropertyAccessor {
        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) {
            return target instanceof Map && ((Map<?, ?>) target).containsKey(name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) {
            return new TypedValue(((Map<?, ?>) target).get(name));
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) {
            return false;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) {
        }

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null;
        }
    }
}
