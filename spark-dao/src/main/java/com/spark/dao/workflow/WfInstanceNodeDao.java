package com.spark.dao.workflow;

import com.spark.common.bean.workflow.entity.WfInstanceNode;
import com.spark.common.bean.workflow.query.WfInstanceNodeQuery;
import com.spark.common.bean.workflow.result.WfInstanceNodeResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * workFlow实例节点运行记录表DAO
 */
public interface WfInstanceNodeDao extends BaseDao<WfInstanceNode> {

    @Override
    int insert(WfInstanceNode node);

    @Override
    int deleteById(WfInstanceNode node);

    @Override
    int updateById(WfInstanceNode node);

    int queryInstanceNodeCount(WfInstanceNodeQuery query);

    List<WfInstanceNodeResult> queryInstanceNodeList(WfInstanceNodeQuery query);

    /**
     * 查询挂起实例上的待审核人工审核节点候选，联表实例过滤租户与实例状态，不分页；审核人匹配由Service解析output_json完成
     * @param query 查询参数，需携带nodeType/status/instanceStatus/tenantId
     * @return 候选节点记录列表，按id倒序
     */
    List<WfInstanceNodeResult> queryPendingReviewNodeList(WfInstanceNodeQuery query);

    @Select("select max(id) from wf_instance_node")
    Long queryInstanceNodeMaxId();
}
