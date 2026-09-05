package com.spark.task.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.task.entity.TaskInstance;
import com.spark.common.bean.task.query.TaskInstanceQuery;
import com.spark.common.bean.task.result.TaskInstanceResult;
import com.spark.common.utils.*;
import com.spark.dao.task.TaskInstanceDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.TaskStatusEnum;
import com.spark.common.enums.TaskTypeEnum;
import com.spark.manage.BaseService;
import com.spark.task.service.ITaskInstanceService;
import com.spark.task.service.ITaskTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 16:30:00
 * 通用定时任务调度服务实现：扫描到期任务并按任务类型分发给处理器
 */
@Service
public class TaskInstanceServiceImpl extends BaseService<TaskInstanceQuery, TaskInstanceResult> implements ITaskInstanceService {
    private final static Logger logger = LoggerFactory.getLogger(TaskInstanceServiceImpl.class);
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private List<ITaskTypeHandler> taskTypeHandlers;

    /**
     * 执行任务
     * @return 执行结果
     */
    @Override
    public ResultData<Void> executeTask() {
        ResultData<Void> result = new ResultData<>();
        TaskInstanceQuery taskInstanceQuery = new TaskInstanceQuery();
        taskInstanceQuery.setPage(false);
        taskInstanceQuery.setStatus(TaskStatusEnum.PENDING.getValue());
        taskInstanceQuery.setTaskTimeEnd(new Date());
        List<TaskInstanceResult> taskInstanceList = taskInstanceDao.queryTaskInstanceList(taskInstanceQuery);
        if (CollectionUtil.isEmpty(taskInstanceList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        Map<Integer, ITaskTypeHandler> handlerMap = new HashMap<>();
        for (ITaskTypeHandler taskTypeHandler : taskTypeHandlers) {
            handlerMap.put(taskTypeHandler.getTaskType(), taskTypeHandler);
        }
        for (TaskInstanceResult taskInstance : taskInstanceList) {
            // 检查是否有前置任务未完成
            boolean hasTask = this.checkHasPrecedingTask(taskInstance);
            if (hasTask) {
                continue;
            }
            ITaskTypeHandler taskTypeHandler = handlerMap.get(taskInstance.getTaskType());
            if (taskTypeHandler == null) {
                TaskInstance updateTask = new TaskInstance();
                updateTask.setId(taskInstance.getId());
                updateTask.setStatus(TaskStatusEnum.FAIL.getValue());
                updateTask.setRemark("未知任务类型");
                int count = taskInstanceDao.updateDBById(updateTask);
                if (count < 1) {
                    logger.error("taskTypeHandler null, update db fail, taskId={}", taskInstance.getId());
                }
                continue;
            }
            try {
                // 构造任务实例所属参数
                Map<String, String> params = this.generateTaskInstanceParams(taskInstance);
                // 执行任务实例
                ResultData<Map<String, String>> handle = taskTypeHandler.handle(taskInstance, params);
                // 处理结果
                TaskInstance updateTask = new TaskInstance();
                updateTask.setId(taskInstance.getId());
                if (handle.getCode() == ResultData.OK) {
                    updateTask.setStatus(TaskStatusEnum.SUCCESS.getValue());
                    Map<String, String> data = handle.getData();
                    if (MapUtil.isNotEmpty(data)) {
                        updateTask.setOutputJson(JsonUtil.toString(data));
                    }
                } else if (handle.getCode() == ErrorCodeEnum.TASK_RESTART.getValue()) {
                    updateTask.setTaskTime(DateUtil.offsetHour(new Date(), taskInstance.getIntervalHours()));
                } else {
                    updateTask.setStatus(TaskStatusEnum.FAIL.getValue());
                    updateTask.setRemark(handle.getMessage());
                }
                int count = taskInstanceDao.updateDBById(updateTask);
            } catch (Exception e) {
                logger.error("executeTask error, taskId={}, taskType={}", taskInstance.getId(), taskInstance.getTaskType(), e);
                TaskInstance updateTask = new TaskInstance();
                updateTask.setId(taskInstance.getId());
                updateTask.setStatus(TaskStatusEnum.FAIL.getValue());
                updateTask.setRemark("处理异常");
                int count = taskInstanceDao.updateDBById(updateTask);
            }
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 检查是否有前置任务未执行
     * @return
     */
    private boolean checkHasPrecedingTask(TaskInstanceResult taskInstance) {
        int precedingCount = taskInstanceDao.countUnfinishedPrecedingTask(taskInstance.getSetId(), taskInstance.getSort());
        return precedingCount > 0;
    }

    /**
     * 分页查询任务实例列表
     * @param query 查询参数
     * @return 任务实例分页结果
     */
    @Override
    public ResultData<PageResult<TaskInstanceResult>> pageTaskInstanceList(TaskInstanceQuery query) {
        ResultData<PageResult<TaskInstanceResult>> result = new ResultData<>();
        if (query == null) {
            query = new TaskInstanceQuery();
        }
        PageResult<TaskInstanceResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询任务实例详情
     * @param query 查询参数
     * @return 任务实例详情
     */
    @Override
    public ResultData<TaskInstanceResult> queryTaskInstanceDetail(TaskInstanceQuery query) {
        ResultData<TaskInstanceResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TaskInstanceResult taskInstanceResult = taskInstanceDao.queryTaskInstance(query);
        if (taskInstanceResult == null) {
            result.setErrorCode(ErrorCodeEnum.TASK_INSTANCE_NOT_EXIST);
            return result;
        }
        List<TaskInstanceResult> supplyList = List.of(taskInstanceResult);
        this.supplyList(supplyList);
        result.setData(taskInstanceResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<TaskInstanceResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        list.forEach(item -> {
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.indexOf(item.getTaskType());
            if (taskTypeEnum != null) {
                item.setTaskTypeName(taskTypeEnum.getDesc());
            }
            item.setStatusName(TaskStatusEnum.indexOf(item.getStatus()).getDesc());
            item.setObjTypeName(ObjectTypeEnum.indexOf(item.getObjType()).getDesc());
        });
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(TaskInstanceQuery query) {
        return taskInstanceDao.queryTaskInstanceCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<TaskInstanceResult> queryList(TaskInstanceQuery query) {
        return taskInstanceDao.queryTaskInstanceList(query);
    }

    /**
     * 构造任务实例参数，从inputJson解析
     * @param taskInstance 任务实例
     * @return 参数Map
     */
    private Map<String, String> generateTaskInstanceParams(TaskInstanceResult taskInstance) {
        Map<String, String> params = new HashMap<>();
        String inputJson = taskInstance.getInputJson();
        if (StringUtil.isNotBlank(inputJson)) {
            Map<String, String> map = JsonUtil.parseMap(inputJson);
            if (MapUtil.isNotEmpty(map)) {
                params.putAll(map);
            }
        }
        TaskInstanceQuery instanceQuery = new TaskInstanceQuery();
        instanceQuery.setSetId(taskInstance.getSetId());
        instanceQuery.setPage(false);
        List<TaskInstanceResult> instanceList = taskInstanceDao.queryTaskInstanceList(instanceQuery);
        if (CollectionUtil.isNotEmpty(instanceList)) {
            List<String> outList = instanceList.stream().filter(v -> StringUtil.isNotBlank(v.getInputJson())).map(TaskInstanceResult::getOutputJson).toList();
            for (String outputJson : outList) {
                Map<String, String> map = JsonUtil.parseMap(outputJson);
                if (MapUtil.isNotEmpty(map)) {
                    params.putAll(map);
                }
            }
        }
        return params;
    }
}
