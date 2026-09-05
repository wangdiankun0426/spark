package com.spark.dao.flow;

import com.spark.common.bean.flow.entity.FlowTemplateNode;
import com.spark.common.bean.flow.query.FlowTemplateNodeQuery;
import com.spark.common.bean.flow.result.FlowTemplateNodeResult;
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
 * @since 2025/11/2 16:52
 */
public interface FlowTemplateNodeDao extends BaseDao<FlowTemplateNode> {
    /**
     * 批量插入
     * @param templateId
     * @param revId
     * @param templateNodes
     * @return
     */
    int batchInsert(@Param("templateId") Long templateId,@Param("revId") Long revId, @Param("list") List<FlowTemplateNode> templateNodes);

    /**
     * 查询流程模板节点
     * @param templateNodeQuery
     * @return
     */
    FlowTemplateNodeResult queryTemplateNode(FlowTemplateNodeQuery templateNodeQuery);

}
