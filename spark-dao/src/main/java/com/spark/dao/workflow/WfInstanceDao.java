package com.spark.dao.workflow;

import com.spark.bean.workflow.entity.WfInstance;
import com.spark.bean.workflow.query.WfInstanceQuery;
import com.spark.bean.workflow.result.WfInstanceResult;
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
 * AI工作流运行实例表DAO
 */
public interface WfInstanceDao extends BaseDao<WfInstance> {

    /**
     * 插入数据
     * @param instance
     * @return
     */
    @Override
    int insert(WfInstance instance);

    /**
     * 删除数据
     * @param instance
     * @return
     */
    @Override
    int deleteById(WfInstance instance);

    /**
     * 修改数据
     * @param instance
     * @return
     */
    @Override
    int updateById(WfInstance instance);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryInstanceCount(WfInstanceQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<WfInstanceResult> queryInstanceList(WfInstanceQuery query);

    /**
     * 查询详情
     * @param query
     * @return
     */
    WfInstanceResult queryInstance(WfInstanceQuery query);

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from wf_instance")
    Long queryInstanceMaxId();
}
