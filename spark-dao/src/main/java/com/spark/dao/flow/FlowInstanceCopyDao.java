package com.spark.dao.flow;

import com.spark.bean.flow.entity.FlowInstanceCopy;
import com.spark.bean.flow.query.FlowInstanceCopyQuery;
import com.spark.bean.flow.result.FlowInstanceCopyResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-27 10:00:00
 */
public interface FlowInstanceCopyDao extends BaseDao<FlowInstanceCopy> {

    /**
     * 插入数据
     * @param copy
     * @return
     */
    @Override
    int insert(FlowInstanceCopy copy);

    /**
     * 根据id修改数据
     * @param copy
     * @return
     */
    @Override
    int updateById(FlowInstanceCopy copy);

    /**
     * 根据id删除数据
     * @param copy
     * @return
     */
    @Override
    int deleteById(FlowInstanceCopy copy);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryCopyCount(FlowInstanceCopyQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FlowInstanceCopyResult> queryCopyList(FlowInstanceCopyQuery query);
}
