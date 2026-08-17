package com.spark.dao.task;

import com.spark.bean.task.entity.TaskInstanceParam;
import com.spark.bean.task.query.TaskInstanceParamQuery;
import com.spark.bean.task.result.TaskInstanceParamResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 17:30:00
 * 通用定时任务参数DAO
 */
public interface TaskInstanceParamDao extends BaseDao<TaskInstanceParam> {

    /**
     * 插入数据
     * @param taskInstanceParam
     * @return
     */
    @Override
    int insert(TaskInstanceParam taskInstanceParam);

    /**
     * 更新数据
     * @param taskInstanceParam
     * @return
     */
    @Override
    int updateById(TaskInstanceParam taskInstanceParam);

    /**
     * 查询数据列表
     * @param query
     * @return
     */
    List<TaskInstanceParamResult> queryTaskInstanceParamList(TaskInstanceParamQuery query);
}
