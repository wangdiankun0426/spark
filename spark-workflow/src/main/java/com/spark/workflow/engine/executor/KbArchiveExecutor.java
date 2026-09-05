package com.spark.workflow.engine.executor;

import com.spark.common.bean.base.ResultData;
import com.spark.common.constant.TaskParamCode;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.dms.service.IDocumentService;
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
 * @since 2026-08-21 23:00:00
 * 知识库归档节点执行器
 */
@Component
public class KbArchiveExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(KbArchiveExecutor.class);
    @Autowired
    private IDocumentService documentService;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() { return WorkflowTemplateTypeEnum.KB_ARCHIVE.getValue(); }

    /**
     * 执行节点
     * @param config 节点配置
     * @param input 当前节点输入
     * @return
     */
    @Override
    public Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap , Map<String, String> showValueMap) {
        Map<String, String> output = new HashMap<>();
        if (config == null) {
            throw new IllegalArgumentException(ErrorCodeEnum.INVALID_PARAM.getDesc());
        }
        Long knowledgeId = MapUtil.getLongVal(config, "knowledgeId");
        if (knowledgeId == null) {
            throw new IllegalArgumentException("知识库归档节点知识库ID参数解析失败");
        }
        String fileCode = MapUtil.getStringVal(config, "fileCode");
        String attIdStr = super.generateFlowValue(fileCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(attIdStr)) {
            throw new IllegalArgumentException("知识库归档节点文件ID解析失败");
        }
        List<Long> attIds = Arrays.stream(attIdStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
        List<Long> docIds = new ArrayList<>();
        for (Long attId : attIds) {
            ResultData<Long> fileResult = documentService.fileDocument(attId, knowledgeId);
            if (fileResult.getCode() != ResultData.OK) {
                throw new IllegalArgumentException("文档归档失败: " + fileResult.getMessage());
            }
            docIds.add(fileResult.getData());
        }
        output.put(nodeId+TaskParamCode.KNOWLEDGE_ID, knowledgeId+"");
        output.put(nodeId+TaskParamCode.FILE_ID, StringUtil.join(docIds, ","));
        logger.info("kbArchive node executed, knowledgeId={}, attIds={}", knowledgeId, attIds);
        return output;
    }
}
