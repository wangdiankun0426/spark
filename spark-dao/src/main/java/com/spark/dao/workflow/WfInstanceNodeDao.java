package com.spark.dao.workflow;

import com.spark.bean.workflow.entity.WfInstanceNode;
import com.spark.bean.workflow.query.WfInstanceNodeQuery;
import com.spark.bean.workflow.result.WfInstanceNodeResult;
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
 * AI工作流实例节点运行记录表DAO
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

    @Select("select max(id) from wf_instance_node")
    Long queryInstanceNodeMaxId();
}
