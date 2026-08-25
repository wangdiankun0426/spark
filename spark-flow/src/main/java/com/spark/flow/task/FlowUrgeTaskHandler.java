package com.spark.flow.task;

import com.spark.bean.base.ResultData;
import com.spark.bean.flow.query.FlowInstanceNodeQuery;
import com.spark.bean.flow.query.FlowInstanceQuery;
import com.spark.bean.flow.result.FlowInstanceNodeResult;
import com.spark.bean.flow.result.FlowInstanceResult;
import com.spark.bean.task.result.TaskInstanceResult;
import com.spark.constant.TaskParamCode;
import com.spark.dao.flow.FlowInstanceDao;
import com.spark.dao.flow.FlowInstanceNodeDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.FlowInstanceStatusEnum;
import com.spark.enums.MessageTypeEnum;
import com.spark.enums.TaskTypeEnum;
import com.spark.flow.service.FlowMessageService;
import com.spark.task.service.ITaskTypeHandler;
import com.spark.utils.MapUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 16:30:00
 * 流程催办定时任务处理器
 */
@Service
public class FlowUrgeTaskHandler implements ITaskTypeHandler {
    private final static Logger logger = LoggerFactory.getLogger(FlowUrgeTaskHandler.class);
    @Autowired
    private FlowInstanceDao instanceDao;
    @Autowired
    private FlowInstanceNodeDao instanceNodeDao;
    @Autowired
    private FlowMessageService flowMessageService;

    /**
     * 处理的任务类型
     * @return 任务类型
     */
    @Override
    public Integer getTaskType() {
        return TaskTypeEnum.FLOW_URGE.getValue();
    }

    /**
     * 执行催办任务
     * @param taskInstance 任务实例
     * @param params 任务参数
     * @return 任务执行结果
     */
    @Override
    public ResultData<Map<String, String>> handle(TaskInstanceResult taskInstance, Map<String, String> params) {
        ResultData<Map<String, String>> result = new ResultData<>();
        logger.info("FlowUrgeTaskHandler taskInstance={},params={}", taskInstance, params);
        String instanceNodeIdStr = MapUtil.getStringVal(params, TaskParamCode.FLOW_INSTANCE_NODE_ID);
        if (!StringUtil.isNumeric(instanceNodeIdStr)) {
            logger.error("instanceNodeId param not exist, taskId={}", taskInstance.getId());
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long instanceNodeId = Long.valueOf(instanceNodeIdStr);
        FlowInstanceNodeQuery instanceNodeQuery = new FlowInstanceNodeQuery();
        instanceNodeQuery.setId(instanceNodeId);
        instanceNodeQuery.setStatus(FlowInstanceStatusEnum.PROCESSING.getValue());
        FlowInstanceNodeResult instanceNodeResult = instanceNodeDao.queryInstanceNode(instanceNodeQuery);
        if (instanceNodeResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setId(taskInstance.getObjId());
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("instance not exist, instanceId={}", taskInstance.getObjId());
            return result;
        }
        // 发送催办通知
        ResultData<Void> sendResult = flowMessageService.sendFlowNotice(instanceResult.getFlowableInstanceId(), MessageTypeEnum.FLOW_URGE.getType());
        logger.info("task success ,taskId={}, instanceId={}", taskInstance.getId(), taskInstance.getObjId());
        result.setCode(ErrorCodeEnum.TASK_RESTART.getValue());
        return result;
    }
}
