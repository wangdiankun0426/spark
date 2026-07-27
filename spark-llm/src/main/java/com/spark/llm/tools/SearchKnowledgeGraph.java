package com.spark.llm.tools;

import com.spark.bean.base.ResultData;
import com.spark.llm.retrieve.Neo4jRetrieve;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 17:00:00
 * 知识图谱检索工具，按 Agent 绑定的知识图谱检索三元组
 */
@Component
public class SearchKnowledgeGraph {
    private static final Logger logger = LoggerFactory.getLogger(SearchKnowledgeGraph.class);
    @Autowired
    private Neo4jRetrieve neo4jRetrieve;
    private List<Long> graphIds;

    /**
     * 为 Agent 创建绑定知识图谱的工具实例
     * @param graphIds 知识图谱 ID 列表
     * @return 绑定 graphIds 的工具实例
     */
    public SearchKnowledgeGraph forAgent(List<Long> graphIds) {
        SearchKnowledgeGraph tool = new SearchKnowledgeGraph();
        tool.neo4jRetrieve = this.neo4jRetrieve;
        tool.graphIds = graphIds;
        return tool;
    }

    /**
     * 知识图谱检索工具
     * @param memoryId 会话记忆 id
     * @param query 用户查询
     * @return 检索到的图谱三元组内容
     */
    @Tool(
        name = "search_kg",
        value = "【知识图谱检索工具】基于知识图谱检索实体间关系。当问题涉及实体关联、多跳关系推理时调用。每个问题只调用一次。"
    )
    public String searchKnowledgeGraph(@ToolMemoryId Object memoryId, String query) {
        logger.info("searchKnowledgeGraph memoryId={}, query={}, graphIds={}", memoryId, query, graphIds);
        try {
            if (memoryId == null) {
                return "检索异常:memoryId为空";
            }
            if (StringUtil.isBlank(query)) {
                return "检索异常:搜索关键词不能为空";
            }
            if (CollectionUtil.isEmpty(graphIds)) {
                return "检索异常:当前Agent未关联知识图谱";
            }
            String question = query.replaceAll("\\n", "").trim();
            ResultData<List<String>> retrieval = neo4jRetrieve.retrieve(question, graphIds);
            if (retrieval.getCode() != ResultData.OK || CollectionUtil.isEmpty(retrieval.getData())) {
                return "根据当前知识图谱，未找到与您的问题相关的关系信息。";
            }
            List<String> triplets = retrieval.getData();
            logger.info("searchKnowledgeGraph triplets size={}", triplets.size());
            return "【图谱检索完成，共 " + triplets.size() + " 条关系】\n\n"
                    + String.join("\n", triplets)
                    + "\n\n【检索结束。请基于以上关系回答，不要再次调用本工具】";
        } catch (Exception e) {
            logger.error("searchKnowledgeGraph err", e);
            return "抱歉，知识图谱检索过程中出现异常";
        }
    }
}
