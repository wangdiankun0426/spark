package com.spark.dao.flow;

import com.spark.bean.flow.entity.FlowTemplate;
import com.spark.bean.flow.query.FlowTemplateQuery;
import com.spark.bean.flow.result.FlowTemplateResult;
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
 * @since 2025-10-29 20:57:44
 */
public interface FlowTemplateDao extends BaseDao<FlowTemplate> {
    /**
     * 插入数据
     * @param template
     * @return
     */
    @Override
    int insert(FlowTemplate template);

    /**
     * 删除数据
     * @param template
     * @return
     */
    @Override
    int deleteById(FlowTemplate template);

    /**
     * 修改数据
     * @param template
     * @return
     */
    @Override
    int updateById(FlowTemplate template);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryTemplateCount(FlowTemplateQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FlowTemplateResult> queryTemplateList(FlowTemplateQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    FlowTemplateResult queryTemplate(FlowTemplateQuery query);

    /**
     * 查询最大ID
     * @return
     */
    @Select("select max(id) from flow_template")
    Long queryTemplateMaxId();
}
