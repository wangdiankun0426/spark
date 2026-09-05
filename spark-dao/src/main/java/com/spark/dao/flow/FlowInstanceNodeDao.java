package com.spark.dao.flow;

import com.spark.common.bean.flow.entity.FlowInstanceNode;
import com.spark.common.bean.flow.query.FlowInstanceNodeQuery;
import com.spark.common.bean.flow.result.FlowInstanceNodeResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/2 18:47
 */
public interface FlowInstanceNodeDao extends BaseDao<FlowInstanceNode> {

    /**
     * 添加流程节点
     *
     * @param node
     * @return
     */
    @Override
    int insert(FlowInstanceNode node);

    /**
     * 更新流程节点
     *
     * @param node
     * @return
     */
    @Override
    int updateById(FlowInstanceNode node);

    /**
     * 查询流程节点
     * @param query
     * @return
     */
    FlowInstanceNodeResult queryInstanceNode(FlowInstanceNodeQuery query);

    /**
     * 查询流程节点列表
     * @param instanceNodeQuery
     * @return
     */
    List<FlowInstanceNodeResult> queryInstanceNodeList(FlowInstanceNodeQuery instanceNodeQuery);
}
