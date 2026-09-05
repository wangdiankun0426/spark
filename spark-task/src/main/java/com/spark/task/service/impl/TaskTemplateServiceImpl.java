package com.spark.task.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.task.entity.TaskTemplate;
import com.spark.common.bean.task.query.TaskTemplateQuery;
import com.spark.common.bean.task.result.TaskTemplateResult;
import com.spark.common.bean.task.vo.TaskTemplateVO;
import com.spark.dao.task.TaskTemplateDao;
import com.spark.dao.task.TaskTemplateParamDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.TaskTypeEnum;
import com.spark.manage.BaseService;
import com.spark.task.service.ITaskTemplateService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 通用定时任务模板服务实现：任务模板与模板参数的增删改查
 */
@Service
public class TaskTemplateServiceImpl extends BaseService<TaskTemplateQuery, TaskTemplateResult> implements ITaskTemplateService {
    private final static Logger logger = LoggerFactory.getLogger(TaskTemplateServiceImpl.class);
    @Autowired
    private TaskTemplateDao taskTemplateDao;
    @Autowired
    private TaskTemplateParamDao taskTemplateParamDao;

    /**
     * 创建任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createTaskTemplate(TaskTemplateVO taskTemplateVO) {
        ResultData<Void> result = new ResultData<>();
        if (taskTemplateVO == null || StringUtil.isBlank(taskTemplateVO.getName()) || taskTemplateVO.getTaskType() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TaskTemplate taskTemplate = new TaskTemplate();
        BeanUtil.copyProperties(taskTemplateVO, taskTemplate);
        int count = taskTemplateDao.insertDB(taskTemplate);
        if (count < 1) {
            logger.error("createTaskTemplate error, insert db fail");
            return result;
        }
        result.setObjId(taskTemplate.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 修改结果
     */
    @Override
    public ResultData<Void> updateTaskTemplate(TaskTemplateVO taskTemplateVO) {
        ResultData<Void> result = new ResultData<>();
        if (taskTemplateVO == null || taskTemplateVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TaskTemplateQuery query = new TaskTemplateQuery();
        query.setId(taskTemplateVO.getId());
        TaskTemplateResult taskTemplateResult = taskTemplateDao.queryTaskTemplate(query);
        if (taskTemplateResult == null) {
            result.setErrorCode(ErrorCodeEnum.TASK_TEMPLATE_NOT_EXIST);
            return result;
        }
        TaskTemplate taskTemplate = new TaskTemplate();
        BeanUtil.copyProperties(taskTemplateVO, taskTemplate);
        int count = taskTemplateDao.updateDBById(taskTemplate);
        if (count < 1) {
            logger.error("updateTaskTemplate error, update db fail");
            return result;
        }
        result.setObjId(taskTemplate.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 删除结果
     */
    @Override
    public ResultData<Void> deleteTaskTemplate(TaskTemplateVO taskTemplateVO) {
        ResultData<Void> result = new ResultData<>();
        if (taskTemplateVO == null || taskTemplateVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        TaskTemplateQuery query = new TaskTemplateQuery();
        query.setId(taskTemplateVO.getId());
        TaskTemplateResult taskTemplateResult = taskTemplateDao.queryTaskTemplate(query);
        if (taskTemplateResult == null) {
            result.setErrorCode(ErrorCodeEnum.TASK_TEMPLATE_NOT_EXIST);
            return result;
        }
        TaskTemplate taskTemplate = new TaskTemplate();
        taskTemplate.setId(taskTemplateVO.getId());
        int count = taskTemplateDao.deleteDBById(taskTemplate);
        if (count < 1) {
            logger.error("deleteTaskTemplate error, delete db fail");
            return result;
        }
        // 级联删除该模板下的参数
        int paramCount = taskTemplateParamDao.deleteByTemplateId(taskTemplate.getId(), userId);
        if (paramCount < 0) {
            logger.error("deleteTaskTemplate error, delete param db fail");
            return result;
        }
        result.setObjId(taskTemplate.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询任务模板列表
     * @param query 查询参数
     * @return 任务模板分页结果
     */
    @Override
    public ResultData<PageResult<TaskTemplateResult>> pageTaskTemplateList(TaskTemplateQuery query) {
        ResultData<PageResult<TaskTemplateResult>> result = new ResultData<>();
        if (query == null) {
            query = new TaskTemplateQuery();
        }
        result.setData(super.pageList(query));
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询任务模板详情
     * @param query 查询参数
     * @return 任务模板
     */
    @Override
    public ResultData<TaskTemplateResult> detailTaskTemplate(TaskTemplateQuery query) {
        ResultData<TaskTemplateResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TaskTemplateResult taskTemplateResult = taskTemplateDao.queryTaskTemplate(query);
        if (taskTemplateResult == null) {
            result.setErrorCode(ErrorCodeEnum.TASK_TEMPLATE_NOT_EXIST);
            return result;
        }
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.indexOf(taskTemplateResult.getTaskType());
        if (taskTypeEnum != null) {
            taskTemplateResult.setTaskTypeName(taskTypeEnum.getDesc());
        }
        result.setData(taskTemplateResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充模板列表数据
     * @param list 模板列表
     */
    @Override
    protected void supplyList(List<TaskTemplateResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        list.forEach(item -> {
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.indexOf(item.getTaskType());
            if (taskTypeEnum != null) {
                item.setTaskTypeName(taskTypeEnum.getDesc());
            }
        });
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(TaskTemplateQuery query) {
        return taskTemplateDao.queryTaskTemplateCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<TaskTemplateResult> queryList(TaskTemplateQuery query) {
        return taskTemplateDao.queryTaskTemplateList(query);
    }
}
