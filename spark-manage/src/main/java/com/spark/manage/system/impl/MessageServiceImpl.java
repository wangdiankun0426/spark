package com.spark.manage.system.impl;

import com.spark.bean.system.entity.Message;
import com.spark.bean.system.query.MessageQuery;
import com.spark.bean.system.query.MessageUserQuery;
import com.spark.bean.system.result.MessageResult;
import com.spark.bean.system.result.MessageUserResult;
import com.spark.bean.system.vo.MessageVO;
import com.spark.bean.base.*;
import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.dao.system.MessageDao;
import com.spark.dao.system.MessageUserDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.MessageTypeEnum;
import com.spark.enums.OperateTypeEnum;
import com.spark.manage.BaseService;
import com.spark.manage.system.IMessageService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-06-18 16:08:03
 */
@Service
public class MessageServiceImpl extends BaseService<MessageQuery, MessageResult> implements IMessageService {
    private final static Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private MessageUserDao messageUserDao;

    /**
     * 创建消息
     * @param messageVO 消息
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createMessage(MessageVO messageVO) {
        ResultData<Void> result = new ResultData<>();
        if (messageVO == null || messageVO.getType() == null || StringUtil.isBlank(messageVO.getTitle())
                || StringUtil.isBlank(messageVO.getContent()) || messageVO.getRefId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            SessionHolder.setCurrentUserId(101L);
        }
        Message message = new Message();
        BeanUtils.copyProperties(messageVO, message);
        int count = messageDao.insertDB(message);
        if (count < 1) {
            return result;
        }
        if (CollectionUtil.isEmpty(messageVO.getUserIds())) {
            result.setCode(ResultData.OK);
            return result;
        }
        count = messageUserDao.batchInsert(message.getId(), messageVO.getUserIds(), SessionHolder.getCurrentUserId());
        if (count < 1) {
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页我的消息列表
     * @return 列表
     */
    @Override
    public ResultData<List<MessageResult>> queryMyMessageList() {
        ResultData<List<MessageResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        MessageUserQuery messageUserQuery = new MessageUserQuery();
        messageUserQuery.setUserId(userId);
        messageUserQuery.setPage(false);
        List<MessageUserResult> messageUserList = messageUserDao.queryMessageUserList(messageUserQuery);
        if (CollectionUtil.isEmpty(messageUserList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        List<Long> msgIds = messageUserList.stream().map(MessageUserResult::getMsgId).distinct().collect(Collectors.toList());
        MessageQuery query = new MessageQuery();
        query.setIds(msgIds);
        List<MessageResult> messageList = messageDao.queryMessageList(query);
        result.setData(messageList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询消息列表
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<MessageResult>> pageMessageList(MessageQuery query) {
        ResultData<PageResult<MessageResult>> result = new ResultData<>();
        if (query == null) {
            query = new MessageQuery();
        }
        result.setData(super.pageList(query));
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 发送消息
     * @param messageVO 消息数据
     * @return 发送结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.MESSAGE_INSERT)
    public ResultData<Void> sendMessage(MessageVO messageVO) {
        ResultData<Void> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (messageVO == null || StringUtil.isBlank(messageVO.getTitle())
                || StringUtil.isBlank(messageVO.getContent()) || CollectionUtil.isEmpty(messageVO.getUserIds())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Message message = new Message();
        BeanUtils.copyProperties(messageVO, message);
        message.setType(MessageTypeEnum.MANUAL.getType());
        message.setRefId(userId);
        int count = messageDao.insertDB(message);
        if (count < 1) {
            return result;
        }
        List<Long> userIds = messageVO.getUserIds().stream().distinct().collect(Collectors.toList());
        count = messageUserDao.batchInsert(message.getId(), userIds, userId);
        if (count < 1) {
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除消息
     * @param messageVO 删除参数
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.MESSAGE_DELETE)
    public ResultData<Void> deleteMessage(MessageVO messageVO) {
        ResultData<Void> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (messageVO == null || messageVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        MessageQuery query = new MessageQuery();
        query.setId(messageVO.getId());
        MessageResult messageResult = messageDao.queryMessage(query);
        if (messageResult == null) {
            result.setErrorCode(ErrorCodeEnum.MESSAGE_NOT_EXIST);
            return result;
        }
        Message message = new Message();
        message.setId(messageVO.getId());
        int count = messageDao.deleteDBById(message);
        if (count < 1) {
            return result;
        }
        count = messageUserDao.deleteByMsgId(messageVO.getId(), userId, new Date());
        if (count < 1) {
            logger.warn("deleteMessage warn, no message user deleted, messageId: {}", messageVO.getId());
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充列表数据
     * @param list 列表数据
     */
    @Override
    protected void supplyList(List<MessageResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        List<Long> msgIds = list.stream().map(MessageResult::getId).toList();
        MessageUserQuery messageUserQuery = new MessageUserQuery();
        messageUserQuery.setMsgIds(msgIds);
        messageUserQuery.setPage(false);
        List<MessageUserResult> messageUserList = messageUserDao.queryMessageUserList(messageUserQuery);
        Map<Long, List<MessageUserResult>> messageUserMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(messageUserList)) {
            messageUserMap = messageUserList.stream().collect(Collectors.groupingBy(MessageUserResult::getMsgId));
        }
        Map<Long, List<MessageUserResult>> finalMessageUserMap = messageUserMap;
        list.forEach(item -> {
            MessageTypeEnum messageTypeEnum = MessageTypeEnum.indexOf(item.getType());
            item.setTypeName(messageTypeEnum.getTitle());
            if (!finalMessageUserMap.containsKey(item.getId())) {
                return;
            }
            List<MessageUserResult> messageUsers = finalMessageUserMap.get(item.getId());
            List<Long> userIds = messageUsers.stream().map(MessageUserResult::getUserId).distinct().toList();
            item.setUserIds(userIds);
            item.setUserNames(super.getObjNames(userIds));
        });
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(MessageQuery query) {
        return messageDao.queryMessageCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<MessageResult> queryList(MessageQuery query) {
        return messageDao.queryMessageList(query);
    }
}
