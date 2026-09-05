package com.spark.workflow.engine.executor;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.AttachmentQuery;
import com.spark.common.bean.sys.result.AttachmentResult;
import com.spark.common.constant.TaskParamCode;
import com.spark.dao.dms.AttachmentDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.manage.BaseService;
import com.spark.common.utils.FileUtil;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import com.spark.common.utils.TextUtil;
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
 * @since 2026-08-21 22:50:00
 * 文档解析节点执行器
 */
@Component
public class DocParseExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(DocParseExecutor.class);
    @Autowired
    private AttachmentDao attachmentDao;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() { return WorkflowTemplateTypeEnum.DOC_PARSE.getValue(); }

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
        String fileCode = MapUtil.getStringVal(config, "fileCode");
        String attIdStr = super.generateFlowValue(fileCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(attIdStr)) {
            throw new IllegalArgumentException("文档解析节点文件ID解析失败");
        }
        List<Long> attIds = Arrays.stream(attIdStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
        for (Long attId : attIds) {
            AttachmentQuery attachmentQuery = new AttachmentQuery();
            attachmentQuery.setId(attId);
            AttachmentResult attachmentResult = attachmentDao.queryAttachment(attachmentQuery);
            if (attachmentResult == null) {
                throw new IllegalArgumentException(ErrorCodeEnum.ATTACHMENT_NOT_EXIST+": " + attId);
            }
            String txtPath = FileUtil.convertToTxt(attachmentResult.getPath());
            ResultData<String> fromText = TextUtil.getFromText(txtPath, true);
            if (fromText.getCode() != ResultData.OK || fromText.getData() == null) {
                throw new IllegalArgumentException("文档内容解析失败: " + attachmentResult.getName());
            }
        }
        logger.info("docParse node executed, file count={}", attIds.size());
        output.put(nodeId+"result", "true");
        output.put(nodeId+TaskParamCode.ATT_ID, StringUtil.join(attIds, ","));
        return output;
    }
}
