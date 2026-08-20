package com.spark.dao.task;

import com.spark.bean.task.entity.TaskTemplate;
import com.spark.bean.task.query.TaskTemplateQuery;
import com.spark.bean.task.result.TaskTemplateResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 通用定时任务模板 DAO
 */
public interface TaskTemplateDao extends BaseDao<TaskTemplate> {

    /**
     * 查询任务模板数量
     * @param query 查询参数
     * @return 数量
     */
    int queryTaskTemplateCount(TaskTemplateQuery query);

    /**
     * 查询任务模板列表
     * @param query 查询参数
     * @return 列表
     */
    List<TaskTemplateResult> queryTaskTemplateList(TaskTemplateQuery query);

    /**
     * 查询任务模板
     * @param query 查询参数
     * @return 任务模板
     */
    TaskTemplateResult queryTaskTemplate(TaskTemplateQuery query);
}
