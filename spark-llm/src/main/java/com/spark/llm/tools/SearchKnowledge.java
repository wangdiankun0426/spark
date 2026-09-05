package com.spark.llm.tools;

import com.spark.common.bean.chat.result.ChatMsgAttResult;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.dao.dms.DocumentDao;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.llm.retrieve.ESRetrieve;
import com.spark.common.bean.llm.result.RetrieveResult;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-17 10:00:00
 * 知识库检索工具（Small-to-Big），按Agent绑定的知识库检索
 */
@Component
public class SearchKnowledge {
    private static final Logger logger = LoggerFactory.getLogger(SearchKnowledge.class);
    @Autowired
    private ESRetrieve esRetrieve;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private DocumentDao documentDao;
    /**
     * 会话级参考文档上下文，key为memoryId（即spaceId）
     * forAgent创建的子实例共享同一引用，便于ChatMsgServiceImpl通过单例读取
     */
    private Map<Object, List<ChatMsgAttResult>> referenceContext = new ConcurrentHashMap<>();
    private List<Long> kbIds;

    /**
     * 为Agent创建绑定知识库的工具实例
     * @param kbIds 知识库ID列表
     * @return 绑定kbIds的工具实例
     */
    public SearchKnowledge forAgent(List<Long> kbIds) {
        SearchKnowledge tool = new SearchKnowledge();
        tool.esRetrieve = this.esRetrieve;
        tool.knowledgeDao = this.knowledgeDao;
        tool.documentDao = this.documentDao;
        tool.kbIds = kbIds;
        tool.referenceContext = this.referenceContext;
        return tool;
    }

    /**
     * 知识库检索工具
     * @param memoryId 会话记忆id
     * @param query 用户查询
     * @return 检索到的知识库内容
     */
    @Tool(
        name = "search_knowledge",
        value = "【重要】此为知识库检索工具。每个问题只调用一次，基于返回内容作答，禁止重复调用。"
    )
    public String searchKnowledge(@ToolMemoryId Object memoryId, String query) {
        logger.info("searchKnowledge memoryId={}, query={}, kbIds={}", memoryId, query, kbIds);
        try {
            if (memoryId == null) {
                return "检索异常:memoryId为空";
            }
            if (StringUtil.isBlank(query)) {
                return "检索异常:搜索关键词不能为空";
            }
            String question = query.replaceAll("\\n", "").trim();
            if (CollectionUtil.isEmpty(kbIds)) {
                return "检索异常:当前Agent未关联知识库";
            }
            KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
            knowledgeQuery.setId(kbIds.get(0));
            KnowledgeResult knowledge = knowledgeDao.queryKnowledge(knowledgeQuery);
            if (knowledge == null) {
                return "检索异常:知识库不存在";
            }
            RetrieveResult retrieval = esRetrieve.retrieve(question, kbIds, knowledge);
            List<String> relevantDocs = retrieval.getContents();
            if (CollectionUtil.isEmpty(relevantDocs)) {
                return "根据当前知识库，无法找到与您的问题相关的内容。";
            }
            List<ChatMsgAttResult> attList = this.generateChatMsgAttList(retrieval.getReferences());
            if (CollectionUtil.isNotEmpty(attList)) {
                referenceContext.put(memoryId, attList);
            }
            logger.info("relevantDocs size={}, attList={}", relevantDocs.size(), attList.size());
            return "【检索完成，共 " + relevantDocs.size() + " 条相关内容】\n\n"
                    + String.join("\n\n", relevantDocs)
                    + "\n\n【检索结束。请基于以上内容回答，不要再次调用本工具】";
        } catch (Exception e) {
            logger.error("searchKnowledge err", e);
            return "抱歉，知识库检索过程中出现异常";
        }
    }

    /**
     * 构造消息附件列表
     * @param docIds 参考文档列表
     * @return 补充docName后的列表
     */
    private List<ChatMsgAttResult> generateChatMsgAttList(List<Long> docIds) {
        if (CollectionUtil.isEmpty(docIds)) {
            return new ArrayList<>();
        }
        docIds = docIds.stream().distinct().collect(Collectors.toList());
        DocumentQuery query = new DocumentQuery();
        query.setIds(docIds);
        List<DocumentResult> docList = documentDao.queryDocumentList(query);
        if (CollectionUtil.isEmpty(docList)) {
            return new ArrayList<>();
        }
        return docList.stream().map(v -> {
            ChatMsgAttResult result = new ChatMsgAttResult();
            result.setDocId(v.getId());
            result.setDocName(v.getName());
            return result;
        }).toList();
    }

    /**
     * 获取指定会话的参考文档
     * @param memoryId 会话记忆id（即spaceId）
     * @return 参考文档列表，无则返回null
     */
    public List<ChatMsgAttResult> getReferences(Object memoryId) {
        if (memoryId == null) {
            return null;
        }
        return referenceContext.get(memoryId);
    }

    /**
     * 清除指定会话的参考文档
     * @param memoryId 会话记忆id（即spaceId）
     */
    public void clearReferences(Object memoryId) {
        if (memoryId == null) {
            return;
        }
        referenceContext.remove(memoryId);
    }
}
