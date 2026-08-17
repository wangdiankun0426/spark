package com.spark.task.service.impl;

import com.spark.bean.base.ResultData;
import com.spark.bean.task.entity.TaskInstance;
import com.spark.bean.task.query.TaskInstanceParamQuery;
import com.spark.bean.task.query.TaskInstanceQuery;
import com.spark.bean.task.result.TaskInstanceParamResult;
import com.spark.bean.task.result.TaskInstanceResult;
import com.spark.dao.task.TaskInstanceDao;
import com.spark.dao.task.TaskInstanceParamDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.TaskStatusEnum;
import com.spark.task.service.ITaskInstanceService;
import com.spark.task.service.ITaskTypeHandler;
import com.spark.utils.CollectionUtil;
import com.spark.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class TaskInstanceServiceImpl implements ITaskInstanceService {
    private final static Logger logger = LoggerFactory.getLogger(TaskInstanceServiceImpl.class);
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private TaskInstanceParamDao taskInstanceParamDao;
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
            ITaskTypeHandler taskTypeHandler = handlerMap.get(taskInstance.getTaskType());
            if (taskTypeHandler == null) {
                logger.error("executeTask error, handler not found, taskType={}, taskId={}", taskInstance.getTaskType(), taskInstance.getId());
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
                Map<String, String> params = this.generateTaskInstanceParams(taskInstance.getId());
                ResultData<Void> handle = taskTypeHandler.handle(taskInstance, params);
                TaskInstance updateTask = new TaskInstance();
                updateTask.setId(taskInstance.getId());
                if (handle.getCode() == ResultData.OK) {
                    updateTask.setStatus(TaskStatusEnum.SUCCESS.getValue());
                } else if (handle.getCode() == ErrorCodeEnum.TASK_RESTART.getValue()) {
                    updateTask.setTaskTime(DateUtil.offsetHour(new Date(), taskInstance.getIntervalHours()));
                } else {
                    updateTask.setStatus(TaskStatusEnum.FAIL.getValue());
                    updateTask.setRemark(result.getMessage());
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
     * 构造任务实例参数
     * @param taskInstanceId 任务实例id
     */
    private Map<String, String> generateTaskInstanceParams(Long taskInstanceId) {
        Map<String, String> params = new HashMap<>();
        TaskInstanceParamQuery paramQuery = new TaskInstanceParamQuery();
        paramQuery.setTaskId(taskInstanceId);
        List<TaskInstanceParamResult> paramList = taskInstanceParamDao.queryTaskInstanceParamList(paramQuery);
        if (CollectionUtil.isEmpty(paramList)) {
            return params;
        }
        params = paramList.stream().collect(Collectors.toMap(TaskInstanceParamResult::getCode, TaskInstanceParamResult::getValue));
        return params;
    }
}
