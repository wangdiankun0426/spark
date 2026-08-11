package com.spark.dao.workflow;

import com.spark.bean.workflow.entity.WfTemplateEndpoint;
import com.spark.bean.workflow.query.WfTemplateEndpointQuery;
import com.spark.bean.workflow.result.WfTemplateEndpointResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 */
public interface WfTemplateEndpointDao extends BaseDao<WfTemplateEndpoint> {

    /**
     * 插入数据
     * @param endpoint
     * @return
     */
    @Override
    int insert(WfTemplateEndpoint endpoint);

    /**
     * 删除数据
     * @param endpoint
     * @return
     */
    @Override
    int deleteById(WfTemplateEndpoint endpoint);

    /**
     * 修改数据
     * @param endpoint
     * @return
     */
    @Override
    int updateById(WfTemplateEndpoint endpoint);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryEndpointCount(WfTemplateEndpointQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<WfTemplateEndpointResult> queryEndpointList(WfTemplateEndpointQuery query);

    /**
     * 查询详情
     * @param query
     * @return
     */
    WfTemplateEndpointResult queryEndpoint(WfTemplateEndpointQuery query);

}
