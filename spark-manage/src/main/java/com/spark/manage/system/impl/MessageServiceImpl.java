package com.spark.manage.system.impl;

import com.spark.bean.system.entity.Message;
import com.spark.bean.system.query.MessageQuery;
import com.spark.bean.system.query.MessageUserQuery;
import com.spark.bean.system.result.MessageResult;
import com.spark.bean.system.result.MessageUserResult;
import com.spark.bean.system.vo.MessageVO;
import com.spark.bean.base.*;
import com.spark.dao.system.MessageDao;
import com.spark.dao.system.MessageUserDao;
import com.spark.enums.ErrorCodeEnum;
import com.spark.manage.system.IMessageService;
import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
public class MessageServiceImpl implements IMessageService {
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
                || StringUtil.isBlank(messageVO.getContent())) {
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
        List<MessageUserResult> messageUserList = messageUserDao.queryMessageUserList(messageUserQuery);
        if (CollectionUtil.isEmpty(messageUserList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        List<Long> msgIds = messageUserList.stream().map(MessageUserResult::getMsgId).distinct().collect(Collectors.toList());
        MessageQuery query = new MessageQuery();
        query.setPage(false);
        // 查询最新的30条消息
        query.setPageSize(30);
        query.setIds(msgIds);
        List<MessageResult> messageList = messageDao.queryMessageList(query);
        result.setData(messageList);
        result.setCode(ResultData.OK);
        return result;
    }

}
