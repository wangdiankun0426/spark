package com.spark.task.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.task.entity.TaskTemplateParam;
import com.spark.common.bean.task.query.TaskTemplateParamQuery;
import com.spark.common.bean.task.result.TaskTemplateParamResult;
import com.spark.common.bean.task.vo.TaskTemplateParamVO;
import com.spark.dao.task.TaskTemplateParamDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.TaskParamTypeEnum;
import com.spark.manage.BaseService;
import com.spark.task.service.ITaskTemplateParamService;
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
 * @since 2026/8/20 18:42
 */
@Service
public class TaskTemplateParamServiceImpl extends BaseService<TaskTemplateParamQuery, TaskTemplateParamResult> implements ITaskTemplateParamService {
    private final static Logger logger = LoggerFactory.getLogger(TaskTemplateParamServiceImpl.class);
    @Autowired
    private TaskTemplateParamDao taskTemplateParamDao;

    /**
     * 查询任务模板参数列表
     * @param query 查询参数
     * @return 参数列表
     */
    @Override
    public ResultData<List<TaskTemplateParamResult>> queryTaskTemplateParamList(TaskTemplateParamQuery query) {
        ResultData<List<TaskTemplateParamResult>> result = new ResultData<>();
        if (query == null || query.getTemplateId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        query.setPage(false);
        List<TaskTemplateParamResult> list = taskTemplateParamDao.queryTaskTemplateParamList(query);
        this.supplyParamList(list);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 创建任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO) {
        ResultData<Void> result = new ResultData<>();
        if (taskTemplateParamVO == null || taskTemplateParamVO.getTemplateId() == null
                || StringUtil.isBlank(taskTemplateParamVO.getName())
                || StringUtil.isBlank(taskTemplateParamVO.getCode())
                || taskTemplateParamVO.getType() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TaskTemplateParam taskTemplateParam = new TaskTemplateParam();
        BeanUtil.copyProperties(taskTemplateParamVO, taskTemplateParam);
        int count = taskTemplateParamDao.insertDB(taskTemplateParam);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(taskTemplateParam.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 修改结果
     */
    @Override
    public ResultData<Void> updateTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO) {
        ResultData<Void> result = new ResultData<>();
        if (taskTemplateParamVO == null || taskTemplateParamVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TaskTemplateParam taskTemplateParam = new TaskTemplateParam();
        BeanUtil.copyProperties(taskTemplateParamVO, taskTemplateParam);
        int count = taskTemplateParamDao.updateDBById(taskTemplateParam);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setObjId(taskTemplateParam.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 删除结果
     */
    @Override
    public ResultData<Void> deleteTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO) {
        ResultData<Void> result = new ResultData<>();
        if (taskTemplateParamVO == null || taskTemplateParamVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        TaskTemplateParam taskTemplateParam = new TaskTemplateParam();
        taskTemplateParam.setId(taskTemplateParamVO.getId());
        int count = taskTemplateParamDao.deleteDBById(taskTemplateParam);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setObjId(taskTemplateParam.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充模板参数列表数据
     * @param list 参数列表
     */
    private void supplyParamList(List<TaskTemplateParamResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        list.forEach(item -> {
            TaskParamTypeEnum paramTypeEnum = TaskParamTypeEnum.indexOf(item.getType());
            if (paramTypeEnum != null) {
                item.setTypeName(paramTypeEnum.getDesc());
            }
        });
    }
}
