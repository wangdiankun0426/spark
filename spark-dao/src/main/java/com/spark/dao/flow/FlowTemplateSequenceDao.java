package com.spark.dao.flow;

import com.spark.bean.flow.entity.FlowTemplateSequence;
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
public interface FlowTemplateSequenceDao extends BaseDao<FlowTemplateSequence> {
    /**
     * 批量插入
     * @param templateId
     * @param revId
     * @param templateSequences
     * @return
     */
    int batchInsert(@Param("templateId") Long templateId, @Param("revId") Long revId, @Param("list") List<FlowTemplateSequence> templateSequences);

}
