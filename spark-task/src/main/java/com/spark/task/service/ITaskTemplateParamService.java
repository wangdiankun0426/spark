package com.spark.task.service;

import com.spark.bean.base.ResultData;
import com.spark.bean.task.query.TaskTemplateParamQuery;
import com.spark.bean.task.result.TaskTemplateParamResult;
import com.spark.bean.task.vo.TaskTemplateParamVO;

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
public interface ITaskTemplateParamService {

    /**
     * 查询任务模板参数列表
     * @param query 查询参数
     * @return 参数列表
     */
    ResultData<List<TaskTemplateParamResult>> queryTaskTemplateParamList(TaskTemplateParamQuery query);

    /**
     * 创建任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 创建结果
     */
    ResultData<Void> createTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO);

    /**
     * 修改任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 修改结果
     */
    ResultData<Void> updateTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO);

    /**
     * 删除任务模板参数
     * @param taskTemplateParamVO 参数数据
     * @return 删除结果
     */
    ResultData<Void> deleteTaskTemplateParam(TaskTemplateParamVO taskTemplateParamVO);

}
