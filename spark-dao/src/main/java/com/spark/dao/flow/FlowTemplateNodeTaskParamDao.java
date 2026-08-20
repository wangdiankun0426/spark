package com.spark.dao.flow;

import com.spark.bean.flow.entity.FlowTemplateNodeTaskParam;
import com.spark.bean.flow.query.FlowTemplateNodeTaskParamQuery;
import com.spark.bean.flow.result.FlowTemplateNodeTaskParamResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:00:00
 * 流程模板节点任务参数 DAO
 */
public interface FlowTemplateNodeTaskParamDao extends BaseDao<FlowTemplateNodeTaskParam> {

    /**
     * 插入数据
     * @param nodeTaskParam
     * @return
     */
    @Override
    int insert(FlowTemplateNodeTaskParam nodeTaskParam);

    /**
     * 删除数据
     * @param nodeTaskParam
     * @return
     */
    @Override
    int deleteById(FlowTemplateNodeTaskParam nodeTaskParam);

    /**
     * 修改数据
     * @param nodeTaskParam
     * @return
     */
    @Override
    int updateById(FlowTemplateNodeTaskParam nodeTaskParam);

    /**
     * 批量插入节点任务参数
     * @param nodeTaskId 节点任务id
     * @param list 参数列表
     * @return
     */
    int batchInsert(@Param("nodeTaskId") Long nodeTaskId, @Param("list") List<FlowTemplateNodeTaskParam> list);

    /**
     * 查询节点任务参数列表
     * @param query 查询条件
     * @return 列表
     */
    List<FlowTemplateNodeTaskParamResult> queryTemplateNodeTaskParamList(FlowTemplateNodeTaskParamQuery query);

}
