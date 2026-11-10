package com.spark.workflow.engine;

import com.spark.common.bean.base.BaseContext;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.vo.MessageVO;
import com.spark.common.bean.workflow.config.RetryConfig;
import com.spark.common.bean.workflow.entity.WfInstance;
import com.spark.common.bean.workflow.entity.WfInstanceNode;
import com.spark.common.bean.workflow.exception.HumanReviewRequiredException;
import com.spark.common.bean.workflow.query.WfInstanceNodeQuery;
import com.spark.common.bean.workflow.result.WfInstanceNodeResult;
import com.spark.dao.workflow.WfInstanceDao;
import com.spark.dao.workflow.WfInstanceNodeDao;
import com.spark.common.enums.MessageTypeEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.common.enums.WorkflowInstanceStatusEnum;
import com.spark.common.enums.WorkflowNodeStatusEnum;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import com.spark.manage.sys.IMessageService;
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
import java.util.stream.Collectors;

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
    @Autowired
    private IMessageService messageService;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 异步执行DAG，登录会话由workflowExecutor线程池装饰器传播
     *
     * @param context
     * @param dagJson
     */
    @Async("workflowExecutor")
    public void executeAsync(BaseContext context, String dagJson) {
        try {
            execute(context, dagJson);
        } catch (Exception e) {
            logger.error("executeAsync error", e);
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
        Map<String, WfInstanceNodeResult> replayRows = new HashMap<>();
        this.runDag(context, dagJson, replayRows);
    }

    /**
     * 异步恢复执行DAG，人工审核通过后按已完成节点记录重放续跑，登录会话由workflowExecutor线程池装饰器传播
     *
     * @param context
     * @param dagJson
     */
    @Async("workflowExecutor")
    public void resumeAsync(BaseContext context, String dagJson) {
        try {
            WfInstance wfInstance = context.getVal(WfInstance.class);
            if (wfInstance == null) {
                logger.error("resumeAsync skip, wfInstance is null");
                return;
            }
            Map<String, WfInstanceNodeResult> replayRows = this.loadReplayRows(wfInstance.getId());
            this.runDag(context, dagJson, replayRows);
        } catch (Exception e) {
            logger.error("resumeAsync error, errMsg={}", e.getMessage(), e);
        }
    }

    /**
     * 执行DAG核心循环，replayRows中存在完成记录的节点只重建变量空间与拓扑记账不重复执行
     *
     * @param context 执行上下文
     * @param dagJson DAG定义
     * @param replayRows 各节点最新一条运行记录
     */
    private void runDag(BaseContext context, String dagJson, Map<String, WfInstanceNodeResult> replayRows) {
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
        Map<String, Object> dag = JsonUtil.toMap(dagJson);
        List<Map<String, Object>> nodes = JsonUtil.getMapList(dag, "nodes");
        List<Map<String, Object>> sequences = JsonUtil.getMapList(dag, "sequences");
        if (sequences == null) {
            sequences = new ArrayList<>();
        }
        // 构建节点
        Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (Map<String, Object> node : nodes) {
            nodeMap.put(MapUtil.getStringVal(node, "id"), node);
        }
        // 构建邻接表和入度
        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        for (String nid : nodeMap.keySet()) {
            adjacency.put(nid, new ArrayList<>());
            inDegree.put(nid, 0);
        }
        for (Map<String, Object> sequence : sequences) {
            String src = MapUtil.getStringVal(sequence, "source");
            String tgt = MapUtil.getStringVal(sequence, "target");
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

                Map<String, Object> node = nodeMap.get(nodeId);
                if (node == null) {
                    continue;
                }
                String nodeType = MapUtil.getStringVal(node, "type");
                String nodeName = MapUtil.getStringVal(node, "name");
                Map<String, Object> config = JsonUtil.getMap(node, "config");
                if (config == null) {
                    config = new LinkedHashMap<>();
                }
                // 重放恢复：已成功节点直接重建变量空间与拓扑记账，网关走向按持久化的后继行状态恢复不重新求值
                WfInstanceNodeResult replayRow = replayRows.get(nodeId);
                if (replayRow != null && WorkflowNodeStatusEnum.SUCCESS.getValue().equals(replayRow.getStatus())) {
                    Map<String, String> replayOutput = JsonUtil.parseMap(replayRow.getOutputJson());
                    if (replayOutput == null) {
                        replayOutput = new HashMap<>();
                    }
                    variableSpace.setNodeOutput(nodeId, replayOutput);
                    executed.add(nodeId);
                    if (WorkflowTemplateTypeEnum.EXCLUSIVE_GATEWAY.getValue().equals(nodeType)) {
                        for (Map<String, Object> sequence : sequences) {
                            if (nodeId.equals(MapUtil.getStringVal(sequence, "source"))) {
                                String targetId = MapUtil.getStringVal(sequence, "target");
                                WfInstanceNodeResult targetRow = replayRows.get(targetId);
                                boolean live = targetRow == null || !WorkflowNodeStatusEnum.SKIPPED.getValue().equals(targetRow.getStatus());
                                this.releaseNode(targetId, live, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue, replayRows);
                            }
                        }
                    } else {
                        for (String neighbor : adjacency.get(nodeId)) {
                            this.releaseNode(neighbor, true, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue, replayRows);
                        }
                    }
                    continue;
                }
                // 重放恢复：已跳过节点只向后继传播死路，不再插记录行
                if (replayRow != null && WorkflowNodeStatusEnum.SKIPPED.getValue().equals(replayRow.getStatus())) {
                    skipped.add(nodeId);
                    for (String neighbor : adjacency.get(nodeId)) {
                        this.releaseNode(neighbor, false, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue, replayRows);
                    }
                    continue;
                }
                // 节点输入，从变量空间获取前置节点输出
                Map<String, String> nodeInput = new HashMap<>();
                for (Map<String, Object> sequence : sequences) {
                    if (nodeId.equals(MapUtil.getStringVal(sequence, "target"))) {
                        String prevNodeId = MapUtil.getStringVal(sequence, "source");
                        Map<String, String> prevOutput = variableSpace.getNodeOutput(prevNodeId);
                        if (prevOutput != null) {
                            nodeInput.putAll(prevOutput);
                        }
                    }
                }
                // 创建节点运行记录
                Long runNodeId = this.insertInstanceNode(instanceId, nodeId, nodeName, nodeType, WorkflowNodeStatusEnum.RUNNING.getValue(), JsonUtil.toString(nodeInput));
                long nodeStart = System.currentTimeMillis();
                // 节点输出（带超时和重试）
                Map<String, String> nodeOutput = new HashMap<>();
                try {
                    IWfNodeExecutor executor = executorMap.get(nodeType);
                    if (executor != null && WorkflowTemplateTypeEnum.HUMAN_REVIEW.getValue().equals(nodeType)) {
                        // 人工审核以异常作为挂起信号，必须绕过超时与重试壳，否则异常被包装后类型丢失、无法进入挂起分支
                        nodeOutput = executor.execute(nodeId, config, nodeInput, valueMap, showValueMap);
                    } else if (executor != null) {
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
                    this.updateInstanceNode(runNodeId, WorkflowNodeStatusEnum.SUCCESS.getValue(), JsonUtil.toString(nodeOutput), null, nodeDuration);
                    // 排他网关：按出边配置的走向条件求值，确定生效分支放行，无命中抛异常
                    if (WorkflowTemplateTypeEnum.EXCLUSIVE_GATEWAY.getValue().equals(nodeType)) {
                        this.routeExclusiveGateway(nodeId, sequences, nodeInput, valueMap, showValueMap, instanceId, nodeMap, remainingInDegree, executed, skipped, liveMarked, queue, replayRows);
                        executed.add(nodeId);
                        continue;
                    }
                } catch (HumanReviewRequiredException e) {
                    // 人工审核节点需要暂停
                    logger.info("human review required, nodeId={}, prompt={}", e.getNodeId(), e.getPrompt());
                    long nodeDuration = System.currentTimeMillis() - nodeStart;
                    // 更新节点状态为等待审核
                    Map<String, String> reviewInfo = new HashMap<>();
                    reviewInfo.put("prompt", e.getPrompt());
                    reviewInfo.put("reviewerIds", e.getReviewerIds() != null ? e.getReviewerIds() : "");
                    this.updateInstanceNode(runNodeId, WorkflowNodeStatusEnum.WAITING.getValue(), JsonUtil.toString(reviewInfo), null, nodeDuration);
                    // 暂停实例，等待人工审核
                    this.updateInstance(instanceId, WorkflowInstanceStatusEnum.PAUSED.getValue(), null, System.currentTimeMillis() - startTime);
                    String reviewPrompt = e.getPrompt();
                    String reviewerIds = e.getReviewerIds();
                    this.notifyReviewers(context, instanceId, reviewPrompt, reviewerIds);
                    return;
                } catch (Exception e) {
                    logger.error("node execution failed, nodeName={}, nodeType={}", nodeName, nodeType, e);
                    long nodeDuration = System.currentTimeMillis() - nodeStart;
                    this.updateInstanceNode(runNodeId, WorkflowNodeStatusEnum.FAILED.getValue(), null, e.getMessage(), nodeDuration);
                    this.updateInstance(instanceId, WorkflowInstanceStatusEnum.FAILED.getValue(), "节点[" + nodeName + "]执行失败: " + e.getMessage(), System.currentTimeMillis() - startTime);
                    return;
                }
                // 减少后继节点入度
                for (String neighbor : adjacency.get(nodeId)) {
                    this.releaseNode(neighbor, true, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue, replayRows);
                }
                executed.add(nodeId);
            }
            long totalDuration = System.currentTimeMillis() - startTime;
            this.updateInstance(instanceId, WorkflowInstanceStatusEnum.SUCCESS.getValue(), null, totalDuration);
        } catch (Exception e) {
            logger.error("dag execution failed, instanceId={}", instanceId, e);
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
     * @param replayRows 各节点最新一条运行记录
     */
    private void routeExclusiveGateway(String nodeId, List<Map<String, Object>> sequences, Map<String, String> nodeInput,
                                       Map<String, String> valueMap, Map<String, String> showValueMap, Long instanceId,
                                       Map<String, Map<String, Object>> nodeMap, Map<String, Integer> remainingInDegree,
                                       Set<String> executed, Set<String> skipped, Set<String> liveMarked, Queue<String> queue,
                                       Map<String, WfInstanceNodeResult> replayRows) {
        List<Map<String, Object>> outgoing = new ArrayList<>();
        for (Map<String, Object> sequence : sequences) {
            if (nodeId.equals(MapUtil.getStringVal(sequence, "source"))) {
                outgoing.add(sequence);
            }
        }
        // 按配置顺序求值各出边 el 表达式走向条件，取第一个命中
        String activeTarget = null;
        for (Map<String, Object> sequence : outgoing) {
            String conditionExpression = MapUtil.getStringVal(sequence, "conditionExpression");
            if (StringUtil.isNotBlank(conditionExpression) && this.matchExpression(conditionExpression, nodeInput, valueMap, showValueMap)) {
                activeTarget = MapUtil.getStringVal(sequence, "target");
                break;
            }
        }
        // 全部不命中
        if (activeTarget == null) {
            throw new IllegalArgumentException("条件分支节点未匹配到任何走向条件");
        }
        // 命中边活跃放行，其余出边按死路处理
        for (Map<String, Object> sequence : outgoing) {
            boolean live = MapUtil.getStringVal(sequence, "target").equals(activeTarget);
            this.releaseNode(MapUtil.getStringVal(sequence, "target"), live, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue, replayRows);
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
     * @param replayRows 各节点最新一条运行记录
     */
    private void releaseNode(String targetId, boolean live, Long instanceId,
                             Map<String, Map<String, Object>> nodeMap, List<Map<String, Object>> sequences,
                             Map<String, Integer> remainingInDegree, Set<String> executed,
                             Set<String> skipped, Set<String> liveMarked, Queue<String> queue,
                             Map<String, WfInstanceNodeResult> replayRows) {
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
            this.skipNode(targetId, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue, replayRows);
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
     * @param replayRows 各节点最新一条运行记录
     */
    private void skipNode(String targetId, Long instanceId,
                          Map<String, Map<String, Object>> nodeMap, List<Map<String, Object>> sequences,
                          Map<String, Integer> remainingInDegree, Set<String> executed,
                          Set<String> skipped, Set<String> liveMarked, Queue<String> queue,
                          Map<String, WfInstanceNodeResult> replayRows) {
        skipped.add(targetId);
        Map<String, Object> node = nodeMap.get(targetId);
        String nodeName = node != null ? MapUtil.getStringVal(node, "name") : targetId;
        String nodeType = node != null ? MapUtil.getStringVal(node, "type") : "";
        // 写一条跳过状态的节点记录，供运行日志展示；重放恢复时已有记录不再重复插入
        if (!replayRows.containsKey(targetId)) {
            this.insertInstanceNode(instanceId, targetId, nodeName, nodeType, WorkflowNodeStatusEnum.SKIPPED.getValue(), null);
        }
        // 向后继传递死路：后继入度归零且无活跃路径到达时同样跳过
        for (Map<String, Object> sequence : sequences) {
            if (targetId.equals(MapUtil.getStringVal(sequence, "source"))) {
                this.releaseNode(MapUtil.getStringVal(sequence, "target"), false, instanceId, nodeMap, sequences, remainingInDegree, executed, skipped, liveMarked, queue, replayRows);
            }
        }
    }

    /**
     * 获取节点的所有出边目标节点
     * @param nodeId 节点ID
     * @param sequences 边数组
     * @return 出边目标节点ID列表
     */
    private List<String> getOutgoingNodes(String nodeId, List<Map<String, Object>> sequences) {
        List<String> outgoing = new ArrayList<>();
        for (Map<String, Object> sequence : sequences) {
            if (nodeId.equals(MapUtil.getStringVal(sequence, "source"))) {
                outgoing.add(MapUtil.getStringVal(sequence, "target"));
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
    private List<String> getIncomingNodes(String nodeId, List<Map<String, Object>> sequences) {
        List<String> incoming = new ArrayList<>();
        for (Map<String, Object> sequence : sequences) {
            if (nodeId.equals(MapUtil.getStringVal(sequence, "target"))) {
                incoming.add(MapUtil.getStringVal(sequence, "source"));
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
                                                         Map<String, Object> config, Map<String, String> nodeInput,
                                                         Map<String, String> valueMap, Map<String, String> showValueMap) {
        Long timeoutMs = MapUtil.getLongVal(config, "timeoutMs", null);
        if (timeoutMs == null || timeoutMs <= 0) {
            timeoutMs = TimeoutStrategy.DEFAULT_TIMEOUT_MS;
        }

        // 重试配置：未配置的字段使用 RetryConfig 默认值
        RetryConfig retryConfig = new RetryConfig();
        if (config.containsKey("maxRetries")) {
            retryConfig.setMaxRetries(MapUtil.getIntegerVal(config, "maxRetries"));
        }
        if (config.containsKey("retryInitialInterval")) {
            retryConfig.setInitialInterval(MapUtil.getLongVal(config, "retryInitialInterval", null));
        }
        if (config.containsKey("retryMultiplier")) {
            retryConfig.setMultiplier(MapUtil.getDoubleVal(config, "retryMultiplier", null));
        }
        if (config.containsKey("retryMaxInterval")) {
            retryConfig.setMaxInterval(MapUtil.getLongVal(config, "retryMaxInterval", null));
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
     * 加载实例各节点最新一条运行记录，用于审核挂起后的重放恢复，同节点多行取id最大者以容忍崩溃残留行
     * @param instanceId 实例ID
     * @return 节点ID到运行记录的映射
     */
    private Map<String, WfInstanceNodeResult> loadReplayRows(Long instanceId) {
        WfInstanceNodeQuery query = new WfInstanceNodeQuery();
        query.setInstanceId(instanceId);
        query.setPage(false);
        List<WfInstanceNodeResult> rows = instanceNodeDao.queryInstanceNodeList(query);
        Map<String, WfInstanceNodeResult> replayRows = new HashMap<>();
        if (rows == null) {
            return replayRows;
        }
        // 默认按id升序返回，逐条覆盖后保留的即为各节点最新行
        for (WfInstanceNodeResult row : rows) {
            replayRows.put(row.getNodeId(), row);
        }
        return replayRows;
    }

    /**
     * 挂起时给审核人发待办消息，尽力而为，失败仅记日志不影响挂起落库
     * @param context 执行上下文
     * @param instanceId 实例ID
     * @param prompt 审核提示
     * @param reviewerIds 审核人ID串
     */
    private void notifyReviewers(BaseContext context, Long instanceId, String prompt, String reviewerIds) {
        try {
            if (StringUtil.isBlank(reviewerIds)) {
                logger.warn("skip review notify, reviewerIds empty, instanceId={}", instanceId);
                return;
            }
            List<Long> userIds = Arrays.stream(reviewerIds.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                logger.warn("skip review notify, no valid reviewer, instanceId={}", instanceId);
                return;
            }
            String templateName = (String) context.getOrDefault("templateName", "");
            MessageVO messageVO = new MessageVO();
            messageVO.setType(MessageTypeEnum.FLOW_TODO.getType());
            messageVO.setTitle("工作流待审核：" + templateName);
            messageVO.setContent(prompt);
            messageVO.setUserIds(userIds);
            messageVO.setRefId(instanceId);
            Long tenantId = SessionHolder.getCurrentTenantId();
            messageVO.setTenantId(tenantId);
            ResultData<Void> messageResult = messageService.createMessage(messageVO);
            if (messageResult.getCode() != ResultData.OK) {
                logger.warn("create review todo message failed, instanceId={}, code={}", instanceId, messageResult.getCode());
            }
        } catch (Exception e) {
            logger.error("notify reviewers error, instanceId={}, errMsg={}", instanceId, e.getMessage(), e);
        }
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
            logger.warn("exclusive gateway condition evaluate failed, expression={}, errMsg={}", expression, e.getMessage());
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
