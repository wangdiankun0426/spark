package com.spark.workflow.engine.executor;

import com.alibaba.fastjson2.JSON;
import com.spark.common.bean.base.ResultData;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.llm.retrieve.Neo4jRetrieve;
import com.spark.manage.BaseService;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-26 21:00:00
 * 知识图谱节点执行器：调用Neo4jRetrieve检索图谱三元组
 */
@Component
public class KgSearchExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(KgSearchExecutor.class);
    @Autowired
    private Neo4jRetrieve neo4jRetrieve;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return WorkflowTemplateTypeEnum.KG_SEARCH.getValue();
    }

    /**
     * 执行节点
     * @param nodeId 节点id
     * @param config 节点配置
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 节点输出
     */
    @Override
    public Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        Map<String, String> output = new HashMap<>();
        if (config == null) {
            throw new IllegalArgumentException(ErrorCodeEnum.INVALID_PARAM.getDesc());
        }
        String queryCode = MapUtil.getStringVal(config, "query");
        String query = super.generateFlowValue(queryCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(query)) {
            throw new IllegalArgumentException("知识图谱节点查询内容解析失败");
        }
        String graphIdsCode = MapUtil.getStringVal(config, "graphId");
        String graphIdsStr = super.generateFlowValue(graphIdsCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(graphIdsStr)) {
            throw new IllegalArgumentException("知识图谱节点未配置图谱");
        }
        List<Long> graphIds = Arrays.stream(graphIdsStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
        try {
            ResultData<List<String>> resultData = neo4jRetrieve.retrieve(query, graphIds);
            if (resultData.getCode() != ResultData.OK || resultData.getData() == null) {
                throw new RuntimeException("知识图谱检索失败: " + resultData.getMessage());
            }
            List<String> triplets = resultData.getData();
            String mergedContent = String.join("\n", triplets);
            output.put(nodeId + "result", mergedContent);
            output.put(nodeId + "content", mergedContent);
            output.put(nodeId + "count", String.valueOf(triplets.size()));
            output.put(nodeId + "json", JSON.toJSONString(triplets));
            logger.info("KnowledgeGraph node executed, query={}, graphIds={}, tripletCount={}", query, graphIds, triplets.size());
        } catch (Exception e) {
            logger.error("KnowledgeGraph execution failed, query={}", query, e);
            throw new RuntimeException("知识图谱检索失败: " + e.getMessage(), e);
        }
        return output;
    }
}
