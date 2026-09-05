package com.spark.dao.flow;

import com.spark.common.bean.flow.entity.FlowInstanceAssignee;
import com.spark.common.bean.flow.query.FlowInstanceAssigneeQuery;
import com.spark.common.bean.flow.result.FlowInstanceAssigneeResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/4 21:18
 */
public interface FlowInstanceAssigneeDao extends BaseDao<FlowInstanceAssignee> {

    /**
     * 插入数据
     * @param instanceAssignee
     * @return
     */
    @Override
    int insert(FlowInstanceAssignee instanceAssignee);

    /**
     * 更新数据
     * @param instanceAssignee
     * @return
     */
    @Override
    int updateById(FlowInstanceAssignee instanceAssignee);

    /**
     * 查询数据列表
     * @param query
     * @return
     */
    List<FlowInstanceAssigneeResult> queryInstanceAssigneeList(FlowInstanceAssigneeQuery query);

    /**
     * 查询数据
     * @param instanceAssigneeQuery
     * @return
     */
    FlowInstanceAssigneeResult queryInstanceAssignee(FlowInstanceAssigneeQuery instanceAssigneeQuery);
}
