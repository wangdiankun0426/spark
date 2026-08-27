package com.spark.workflow.engine.executor;

import com.spark.bean.base.ResultData;
import com.spark.bean.system.query.AttachmentQuery;
import com.spark.bean.system.result.AttachmentResult;
import com.spark.dao.system.AttachmentDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.WorkflowTemplateTypeEnum;
import com.spark.llm.model.ModelFactory;
import com.spark.manage.BaseService;
import com.spark.utils.*;
import dev.langchain4j.model.chat.ChatModel;
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
 * @since 2026-08-11 15:00:00
 * LLM节点执行器：提示词支持 #{doc:来源}# 变量引用文档分片内容
 * 来源支持 form:表单字段 / node:节点输出 / 固定文档ID，多个文档ID以逗号分隔
 */
@Component
public class LlmTaskExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(LlmTaskExecutor.class);
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private AttachmentDao attachmentDao;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() { return WorkflowTemplateTypeEnum.LLM_TASK.getValue(); }

    /**
     * 执行节点
     * @param nodeId 节点id
     * @param config 节点配置
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return
     */
    @Override
    public Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap , Map<String, String> showValueMap) {
        Map<String, String> output = new HashMap<>();
        if (config == null) {
            throw new IllegalArgumentException(ErrorCodeEnum.INVALID_PARAM.getDesc());
        }
        Long modelId = MapUtil.getLongVal(config, "modelId");
        if (modelId == null) {
            throw new IllegalArgumentException("LLM节点未配置大模型");
        }
        Map<String, String> defParamMap = new HashMap<>();
        String fileCode = MapUtil.getStringVal(config, "fileCode");
        String attIdStr = super.generateFlowValue(fileCode, null, valueMap, showValueMap, input);
        if (StringUtil.isNotBlank(attIdStr)) {
            List<Long> attIds = Arrays.stream(attIdStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
            AttachmentQuery attachmentQuery = new AttachmentQuery();
            attachmentQuery.setId(attIds.get(0));
            AttachmentResult attachmentResult = attachmentDao.queryAttachment(attachmentQuery);
            String filePath = attachmentResult.getPath();
            String txtPath = FileUtil.generateTxtFile(filePath);
            ResultData<String> txtData = TextUtil.getFromText(txtPath, true);
            if (txtData.getCode() == ResultData.OK) {
                defParamMap.put("content", txtData.getData());
            }
        }
        String promptStr = MapUtil.getStringVal(config, "prompt");
        String prompt = super.generateFlowValue(promptStr, defParamMap, valueMap, showValueMap, input);
        if (StringUtil.isBlank(prompt)) {
            throw new IllegalArgumentException("LLM节点系统提示词参数解析失败");
        }
        try {
            ChatModel model = modelFactory.getChatModel(modelId);
            String response = model.chat(prompt);
            output.put(nodeId+"text", response);
            boolean validJson = JsonUtil.isValidJson(response);
            if (validJson) {
                HashMap<String, Object> map = JsonUtil.toObject(response, HashMap.class);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    output.put(nodeId+key, String.valueOf(value));
                }
            }
            logger.info("LLM node executed");
        } catch (Exception e) {
            logger.error("LLM execution failed", e);
            output.put(nodeId+"text", "");
        }
        return output;
    }

}
