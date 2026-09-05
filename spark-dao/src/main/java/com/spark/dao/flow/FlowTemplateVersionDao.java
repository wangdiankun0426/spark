package com.spark.dao.flow;

import com.spark.common.bean.flow.entity.FlowTemplateVersion;
import com.spark.common.bean.flow.query.FlowTemplateVersionQuery;
import com.spark.common.bean.flow.result.FlowTemplateVersionResult;
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
 * @since 2025-11-01 13:34:43
 */
public interface FlowTemplateVersionDao extends BaseDao<FlowTemplateVersion> {
    /**
     * 插入数据
     * @param templateVersion
     * @return
     */
    @Override
    int insert(FlowTemplateVersion templateVersion);

    /**
     * 删除数据
     * @param templateVersion
     * @return
     */
    @Override
    int deleteById(FlowTemplateVersion templateVersion);

    /**
     * 修改数据
     * @param templateVersion
     * @return
     */
    @Override
    int updateById(FlowTemplateVersion templateVersion);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryTemplateVersionCount(FlowTemplateVersionQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<FlowTemplateVersionResult> queryTemplateVersionList(FlowTemplateVersionQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    FlowTemplateVersionResult queryTemplateVersion(FlowTemplateVersionQuery query);

    /**
     * 查询最大版本号
     * @param templateId
     * @return
     */
    @Select("select max(rev_code) from flow_template_version where template_id = #{value}")
    int queryMaxRevCode(Long templateId);
}
