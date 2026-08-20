package com.spark.dao.flow;

import com.spark.bean.flow.entity.FlowTemplateNodeTask;
import com.spark.bean.flow.query.FlowTemplateNodeTaskQuery;
import com.spark.bean.flow.result.FlowTemplateNodeTaskResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:00:00
 * 流程模板节点任务 DAO
 */
public interface FlowTemplateNodeTaskDao extends BaseDao<FlowTemplateNodeTask> {

    /**
     * 插入数据
     * @param nodeTask
     * @return
     */
    @Override
    int insert(FlowTemplateNodeTask nodeTask);

    /**
     * 删除数据
     * @param nodeTask
     * @return
     */
    @Override
    int deleteById(FlowTemplateNodeTask nodeTask);

    /**
     * 修改数据
     * @param nodeTask
     * @return
     */
    @Override
    int updateById(FlowTemplateNodeTask nodeTask);

    /**
     * 查询节点任务列表
     * @param query 查询条件
     * @return 列表
     */
    List<FlowTemplateNodeTaskResult> queryTemplateNodeTaskList(FlowTemplateNodeTaskQuery query);

}
