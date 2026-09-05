package com.spark.task.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.task.query.TaskTemplateQuery;
import com.spark.common.bean.task.result.TaskTemplateResult;
import com.spark.common.bean.task.vo.TaskTemplateVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 通用定时任务模板服务接口
 */
public interface ITaskTemplateService {

    /**
     * 创建任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 创建结果
     */
    ResultData<Void> createTaskTemplate(TaskTemplateVO taskTemplateVO);

    /**
     * 修改任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 修改结果
     */
    ResultData<Void> updateTaskTemplate(TaskTemplateVO taskTemplateVO);

    /**
     * 删除任务模板
     * @param taskTemplateVO 任务模板数据
     * @return 删除结果
     */
    ResultData<Void> deleteTaskTemplate(TaskTemplateVO taskTemplateVO);

    /**
     * 分页查询任务模板列表
     * @param query 查询参数
     * @return 任务模板分页结果
     */
    ResultData<PageResult<TaskTemplateResult>> pageTaskTemplateList(TaskTemplateQuery query);

    /**
     * 查询任务模板详情
     * @param query 查询参数
     * @return 任务模板
     */
    ResultData<TaskTemplateResult> detailTaskTemplate(TaskTemplateQuery query);
}
