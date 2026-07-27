package com.spark.dao.flow;

import com.spark.bean.flow.entity.FlowInstance;
import com.spark.bean.flow.query.FlowInstanceQuery;
import com.spark.bean.flow.result.FlowInstanceResult;
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
 * @since 2025-11-02 15:47:22
 */
public interface FlowInstanceDao extends BaseDao<FlowInstance> {
    /**
     * 插入数据
     * @param instance
     * @return
     */
    @Override
    int insert(FlowInstance instance);

    /**
     * 删除数据
     * @param instance
     * @return
     */
    @Override
    int deleteById(FlowInstance instance);

    /**
     * 修改数据
     * @param instance
     * @return
     */
    @Override
    int updateById(FlowInstance instance);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryInstanceCount(FlowInstanceQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FlowInstanceResult> queryInstanceList(FlowInstanceQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    FlowInstanceResult queryInstance(FlowInstanceQuery query);

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from flow_instance")
    Long queryInstanceMaxId();

    /**
     * 根据实例id更新数据
     * @param instance
     * @return
     */
    int updateByFlowableInstanceId(FlowInstance instance);
}
