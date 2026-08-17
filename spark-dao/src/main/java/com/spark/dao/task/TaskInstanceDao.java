package com.spark.dao.task;

import com.spark.bean.task.entity.TaskInstance;
import com.spark.bean.task.query.TaskInstanceQuery;
import com.spark.bean.task.result.TaskInstanceResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-17 16:30:00
 * 通用定时任务实例DAO
 */
public interface TaskInstanceDao extends BaseDao<TaskInstance> {

    /**
     * 插入数据
     * @param taskInstance
     * @return
     */
    @Override
    int insert(TaskInstance taskInstance);

    /**
     * 更新数据
     * @param taskInstance
     * @return
     */
    @Override
    int updateById(TaskInstance taskInstance);

    /**
     * 查询数据列表
     * @param query
     * @return
     */
    List<TaskInstanceResult> queryTaskInstanceList(TaskInstanceQuery query);
}
