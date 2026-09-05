package com.spark.dao.workflow;

import com.spark.common.bean.workflow.entity.WfTemplateVersion;
import com.spark.common.bean.workflow.query.WfTemplateVersionQuery;
import com.spark.common.bean.workflow.result.WfTemplateVersionResult;
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
 * workFlow模板版本表DAO
 */
public interface WfTemplateVersionDao extends BaseDao<WfTemplateVersion> {

    /**
     * 插入数据
     * @param version
     * @return
     */
    @Override
    int insert(WfTemplateVersion version);

    /**
     * 删除数据
     * @param version
     * @return
     */
    @Override
    int deleteById(WfTemplateVersion version);

    /**
     * 修改数据
     * @param version
     * @return
     */
    @Override
    int updateById(WfTemplateVersion version);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryVersionCount(WfTemplateVersionQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<WfTemplateVersionResult> queryVersionList(WfTemplateVersionQuery query);

    /**
     * 查询详情
     * @param query
     * @return
     */
    WfTemplateVersionResult queryVersion(WfTemplateVersionQuery query);

    /**
     * 查询最大版本号
     * @param templateId
     * @return
     */
    @Select("select max(rev_code) from wf_template_version where template_id = #{templateId}")
    Integer queryMaxRevCode(Long templateId);
}
