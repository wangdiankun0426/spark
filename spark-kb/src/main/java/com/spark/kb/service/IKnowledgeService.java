package com.spark.kb.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.kb.vo.KnowledgeVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-17 10:00:00
 */
public interface IKnowledgeService {

    /**
     * 创建知识库
     * @param knowledgeVO 知识库数据
     * @return 创建结果
     */
    ResultData<Void> createKnowledge(KnowledgeVO knowledgeVO);

    /**
     * 修改知识库
     * @param knowledgeVO 知识库数据
     * @return 修改结果
     */
    ResultData<Void> updateKnowledge(KnowledgeVO knowledgeVO);

    /**
     * 删除知识库
     * @param knowledgeVO 知识库数据
     * @return 删除结果
     */
    ResultData<Void> deleteKnowledge(KnowledgeVO knowledgeVO);

    /**
     * 分页查询知识库
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<KnowledgeResult>> pageKnowledgeList(KnowledgeQuery query);

    /**
     * 查询知识库详情
     * @param query 查询条件
     * @return 详情
     */
    ResultData<KnowledgeResult> queryKnowledgeDetail(KnowledgeQuery query);
}
