package com.spark.flow.service.impl;

import com.spark.bean.base.ResultData;
import com.spark.bean.flow.query.*;
import com.spark.bean.flow.result.*;
import com.spark.bean.system.vo.MessageVO;
import com.spark.config.rabbitmq.MqProducer;
import com.spark.dao.flow.*;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.FlowInstanceStatusEnum;
import com.spark.enums.MessageTypeEnum;
import com.spark.enums.VariableTypeEnum;
import com.spark.flow.service.BaseFlowService;
import com.spark.flow.service.FlowMessageService;
import com.spark.manage.BaseService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.JsonUtil;
import com.spark.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/6 21:34
 */
@Service
public class FlowMessageServiceImpl extends BaseFlowService implements FlowMessageService {
    private final static Logger logger = LoggerFactory.getLogger(FlowMessageServiceImpl.class);
    @Autowired
    private FlowInstanceDao instanceDao;
    @Autowired
    private FlowTemplateMsgDao templateMsgDao;
    @Autowired
    private MqProducer mqProducer;

    /**
     * 发送流程通知
     * @param instanceId
     * @param msgType
     * @return
     */
    @Override
    public ResultData<Void> sendFlowNotice(String instanceId, Integer msgType) {
        ResultData<Void> result = new ResultData<>();
        if (StringUtil.isBlank(instanceId) || msgType == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        FlowInstanceQuery instanceQuery = new FlowInstanceQuery();
        instanceQuery.setFlowableInstanceId(instanceId);
        FlowInstanceResult instanceResult = instanceDao.queryInstance(instanceQuery);
        if (instanceResult == null) {
            logger.error("sendFlowNotice error, instance not exist, instanceId={}",instanceId);
            return result;
        }
        Long templateId = instanceResult.getTemplateId();
        Long revId = instanceResult.getTemplateRevId();
        FlowTemplateMsgQuery msgQuery = new FlowTemplateMsgQuery();
        msgQuery.setTemplateId(templateId);
        msgQuery.setRevId(revId);
        msgQuery.setType(msgType);
        FlowTemplateMsgResult msgResult = templateMsgDao.queryFlowTemplateMsg(msgQuery);
        if (msgResult == null || msgResult.getEnabled() == null || !msgResult.getEnabled()) {
            logger.error("msgResult is null or disable");
            return result;
        }
        String content = msgResult.getContent();
        String recipient = msgResult.getRecipient();
        if (StringUtil.isBlank(content) || StringUtil.isBlank(recipient)) {
            logger.error("sendFlowNotice error, content or recipient not exist");
            return result;
        }
        Map<String, String> defaultParam = super.generateFlowDefaultParam(instanceId);
        content = super.generateFlowValue(content, defaultParam, null, null);
        recipient = super.generateFlowValue(recipient, defaultParam, null, null);
        if (StringUtil.isBlank(recipient)) {
            logger.error("sendFlowNotice error, recipient not exist");
            return result;
        }
        List<Long> userIds = Arrays.stream(recipient.split(",")).filter(StringUtil::isNumeric).map(Long::parseLong).toList();
        MessageVO messageVO = new MessageVO();
        MessageTypeEnum messageType = MessageTypeEnum.indexOf(msgType);
        messageVO.setType(messageType.getType());
        messageVO.setTitle(messageType.getTitle());
        messageVO.setContent(content);
        messageVO.setUserIds(userIds);
        messageVO.setRefId(instanceResult.getId());
        mqProducer.sendSystemMessageMq(JsonUtil.toString(messageVO));
        logger.info("sendFlowNotice success, noticeType={}, userIds={}", messageType, userIds);
        result.setCode(ResultData.OK);
        return result;
    }

}
