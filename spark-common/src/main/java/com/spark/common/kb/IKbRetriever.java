package com.spark.common.kb;

import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.llm.result.RetrieveResult;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 15:00:00
 * 知识库召回能力接口，实现位于知识库模块，供模型层与工作流按接口注入
 * 放这里解决kb包和llm包循环依赖问题
 */
public interface IKbRetriever {

    /**
     * 召回
     * @param query 查询文本
     * @param kbIds 知识库ID列表
     * @param knowledge 知识库配置
     * @return 检索结果（含给LLM的文本和给前端的参考文档）
     */
    RetrieveResult retrieve(String query, List<Long> kbIds, KnowledgeResult knowledge);
}
