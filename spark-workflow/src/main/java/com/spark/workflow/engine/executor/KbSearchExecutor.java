package com.spark.workflow.engine.executor;

import com.alibaba.fastjson2.JSON;
import com.spark.bean.llm.result.RetrieveResult;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.WorkflowTemplateTypeEnum;
import com.spark.llm.retrieve.ESRetrieve;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.MapUtil;
import com.spark.utils.StringUtil;
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
 * @since 2026-08-26 17:00:00
 * 知识库检索节点执行器：调用ESRetrieve进行知识检索
 */
@Component
public class KbSearchExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(KbSearchExecutor.class);

    @Autowired
    private ESRetrieve esRetrieve;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return WorkflowTemplateTypeEnum.KB_SEARCH.getValue();
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
            throw new IllegalArgumentException("RAG节点查询文本解析失败");
        }
        String kbIdsCode = MapUtil.getStringVal(config, "kbIds");
        String kbIdsStr = super.generateFlowValue(kbIdsCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(kbIdsStr)) {
            throw new IllegalArgumentException("RAG节点知识库ID解析失败");
        }
        List<Long> kbIds = Arrays.stream(kbIdsStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
        Integer topK = MapUtil.getIntegerVal(config, "topK", 5);
        try {
            RetrieveResult result = esRetrieve.retrieve(query, kbIds, null);
            List<String> contents = result.getContents();
            if (CollectionUtil.isEmpty(contents)) {
                contents = new ArrayList<>();
            }
            if (contents.size() > topK) {
                contents = contents.subList(0, topK);
            }
            String mergedContent = String.join("\n\n---\n\n", contents);
            output.put(nodeId + "result", mergedContent);
            output.put(nodeId + "content", mergedContent);
            output.put(nodeId + "count", String.valueOf(contents.size()));
            List<Long> references = result.getReferences();
            if (CollectionUtil.isNotEmpty(references)) {
                output.put(nodeId + "references", StringUtil.join(references, ","));
            }
            output.put(nodeId + "json", JSON.toJSONString(result));
            logger.info("RAG node executed, query={}, kbIds={}, resultCount={}", query, kbIds, contents.size());
        } catch (Exception e) {
            logger.error("RAG execution failed, query={}", query, e);
            throw new RuntimeException("知识库检索失败: " + e.getMessage(), e);
        }
        return output;
    }
}
