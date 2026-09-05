package com.spark.dao.workflow;

import com.spark.common.bean.workflow.entity.WfTemplate;
import com.spark.common.bean.workflow.query.WfTemplateQuery;
import com.spark.common.bean.workflow.result.WfTemplateResult;
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
 * workFlow模板表DAO
 */
public interface WfTemplateDao extends BaseDao<WfTemplate> {

    /**
     * 插入数据
     * @param template
     * @return
     */
    @Override
    int insert(WfTemplate template);

    /**
     * 删除数据
     * @param template
     * @return
     */
    @Override
    int deleteById(WfTemplate template);

    /**
     * 修改数据
     * @param template
     * @return
     */
    @Override
    int updateById(WfTemplate template);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryTemplateCount(WfTemplateQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<WfTemplateResult> queryTemplateList(WfTemplateQuery query);

    /**
     * 查询详情
     * @param query
     * @return
     */
    WfTemplateResult queryTemplate(WfTemplateQuery query);

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from wf_template")
    Long queryTemplateMaxId();
}
