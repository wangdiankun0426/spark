package com.spark.flow.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.flow.query.FlowInstanceQuery;
import com.spark.common.bean.flow.query.FlowTemplateMsgQuery;
import com.spark.common.bean.flow.result.FlowInstanceResult;
import com.spark.common.bean.flow.result.FlowTemplateMsgResult;
import com.spark.common.bean.form.query.FormObjValueQuery;
import com.spark.common.bean.form.result.FormObjValueResult;
import com.spark.common.bean.sys.vo.MessageVO;
import com.spark.config.rabbitmq.MqProducer;
import com.spark.dao.flow.*;
import com.spark.dao.form.FormObjValueDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.MessageTypeEnum;
import com.spark.flow.service.BaseFlowService;
import com.spark.flow.service.FlowMessageService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private FormObjValueDao objValueDao;

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
        Map<String, String> valueMap = new HashMap<>();
        Map<String, String> showValueMap = new HashMap<>();
        FormObjValueQuery objValueQuery = new FormObjValueQuery();
        objValueQuery.setObjId(instanceResult.getId());
        List<FormObjValueResult> objValueList = objValueDao.queryFormObjValueList(objValueQuery);
        if (CollectionUtil.isNotEmpty(objValueList)) {
            valueMap = objValueList.stream().collect(Collectors.toMap(FormObjValueResult::getCode, FormObjValueResult::getValue));
            showValueMap = objValueList.stream().collect(Collectors.toMap(FormObjValueResult::getCode, FormObjValueResult::getShowValue));
        }
        content = super.generateFlowValue(content, defaultParam, valueMap, showValueMap, null);
        recipient = super.generateFlowValue(recipient, defaultParam, valueMap, showValueMap, null);
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
        messageVO.setTenantId(103L);
        mqProducer.sendSystemMessageMq(JsonUtil.toString(messageVO));
        logger.info("sendFlowNotice success, noticeType={}, userIds={}", messageType, userIds);
        result.setCode(ResultData.OK);
        return result;
    }

}
