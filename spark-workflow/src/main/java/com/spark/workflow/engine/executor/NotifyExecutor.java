package com.spark.workflow.engine.executor;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.vo.MessageVO;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.WorkflowTemplateTypeEnum;
import com.spark.common.enums.MessageTypeEnum;
import com.spark.manage.BaseService;
import com.spark.manage.sys.IMessageService;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-21 22:55:00
 * 发送通知节点执行器
 */
@Component
public class NotifyExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(NotifyExecutor.class);
    @Autowired
    private IMessageService messageService;

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() { return WorkflowTemplateTypeEnum.NOTIFY.getValue(); }

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
        String titleCode = MapUtil.getStringVal(config, "titleCode");
        String title = super.generateFlowValue(titleCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(title)) {
            throw new IllegalArgumentException("发送通知节点标题参数解析失败");
        }
        String contentCode = MapUtil.getStringVal(config, "contentCode");
        String content = super.generateFlowValue(contentCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(content)) {
            throw new IllegalArgumentException("发送通知节点内容参数解析失败");
        }
        String userCode = MapUtil.getStringVal(config, "userCode");
        String userIdStr = super.generateFlowValue(userCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(userIdStr)) {
            throw new IllegalArgumentException("发送通知节点接收人参数解析失败");
        }
        List<Long> userIds = Arrays.stream(userIdStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).collect(Collectors.toList());
        String refCode = MapUtil.getStringVal(config, "refCode");
        String refCodeStr = super.generateFlowValue(refCode, null, valueMap, showValueMap, input);
        List<Long> refIds = new ArrayList<>();
        if (StringUtil.isNotBlank(refCodeStr)) {
            refIds = Arrays.stream(refCodeStr.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
        } else {
            refIds.add(0L);
        }
        for (Long refId : refIds) {
            MessageVO messageVO = new MessageVO();
            messageVO.setType(MessageTypeEnum.MANUAL.getType());
            messageVO.setTitle(title);
            messageVO.setContent(content);
            messageVO.setUserIds(userIds);
            messageVO.setRefId(refId);
            ResultData<Void> messageResult = messageService.createMessage(messageVO);
            if (messageResult.getCode() != ResultData.OK) {
                throw new IllegalArgumentException("发送通知失败: " + messageResult.getMessage());
            }
        }
        logger.info("notify node executed, refIds={}, user count={}", refIds, userIds.size());
        return output;
    }
}
