package com.spark.dao.flow;

import com.spark.bean.flow.entity.FlowTemplateMsg;
import com.spark.bean.flow.query.FlowTemplateMsgQuery;
import com.spark.bean.flow.result.FlowTemplateMsgResult;
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
 * @since 2026-08-06 10:00:00
 */
public interface FlowTemplateMsgDao extends BaseDao<FlowTemplateMsg> {

    /**
     * 插入数据
     * @param templateMsg
     * @return
     */
    @Override
    int insert(FlowTemplateMsg templateMsg);

    /**
     * 删除数据
     * @param templateMsg
     * @return
     */
    @Override
    int deleteById(FlowTemplateMsg templateMsg);

    /**
     * 修改数据
     * @param templateMsg
     * @return
     */
    @Override
    int updateById(FlowTemplateMsg templateMsg);

    /**
     * 批量插入消息通知配置
     * @param templateId 模板id
     * @param revId 版本id
     * @param list 通知配置列表
     * @return
     */
    int batchInsert(@Param("templateId") Long templateId, @Param("revId") Long revId, @Param("list") List<FlowTemplateMsg> list);

    /**
     * 查询消息通知配置列表
     * @param query 查询条件
     * @return 列表
     */
    List<FlowTemplateMsgResult> queryFlowTemplateMsgList(FlowTemplateMsgQuery query);

    /**
     * 查询单条消息通知配置
     * @param query 查询条件
     * @return 单条数据
     */
    FlowTemplateMsgResult queryFlowTemplateMsg(FlowTemplateMsgQuery query);

}