package com.spark.dao.task;

import com.spark.bean.task.entity.TaskInstanceData;
import com.spark.bean.task.query.TaskInstanceDataQuery;
import com.spark.bean.task.result.TaskInstanceDataResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-21 15:30:00
 * 任务产出数据DAO
 */
public interface TaskInstanceDataDao extends BaseDao<TaskInstanceData> {

    /**
     * 批量插入数据
     * @param list 产出数据列表
     * @return
     */
    int batchInsert(List<TaskInstanceData> list);

    /**
     * 查询数据列表
     * @param query
     * @return
     */
    List<TaskInstanceDataResult> queryTaskInstanceDataList(TaskInstanceDataQuery query);
}
