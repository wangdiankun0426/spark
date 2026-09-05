package com.spark.dao.flow;

import com.spark.common.bean.flow.entity.FlowInstanceDiscuss;
import com.spark.common.bean.flow.query.FlowInstanceDiscussQuery;
import com.spark.common.bean.flow.result.FlowInstanceDiscussResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/3 21:56
 */
public interface FlowInstanceDiscussDao extends BaseDao<FlowInstanceDiscuss> {

    /**
     * 插入数据
     * @param discuss
     * @return
     */
    @Override
    int insert(FlowInstanceDiscuss discuss);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FlowInstanceDiscussResult> queryInstanceDiscussList(FlowInstanceDiscussQuery query);
}
