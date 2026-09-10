package com.spark.chat.service.impl;

import com.spark.config.aspectj.annotation.DataScope;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.chat.query.ChatMsgQuery;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.ChatSpaceResult;
import com.spark.common.bean.chat.result.ChatUserResult;
import com.spark.dao.sys.UserDao;
import com.spark.dao.chat.ChatMsgDao;
import com.spark.dao.chat.ChatSpaceDao;
import com.spark.common.enums.ChatMsgStatusEnum;
import com.spark.common.enums.ChatSpaceTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.chat.service.IChatUserService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.BeanUtil;
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
 * @since 2024/4/17 20:01
 */
@Service
public class ChatUserServiceImpl implements IChatUserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private ChatSpaceDao chatSpaceDao;
    @Autowired
    private ChatMsgDao chatMsgDao;

    /**
     * 查询我的聊天用户列表
     * @param query 查询条件
     * @return 结果
     */
    @Override
    @DataScope
    public ResultData<List<ChatUserResult>> queryMyChatUserList(UserQuery query) {
        ResultData<List<ChatUserResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (query == null) {
            query = new UserQuery();
        }
        query.setTenantId(SessionHolder.getCurrentTenantId());
        List<UserResult> userList = userDao.queryUserList(query);
        if (CollectionUtil.isEmpty(userList)) {
            result.setCode(ResultData.OK);
            return result;
        }
        List<ChatUserResult> list  = new ArrayList<>();
        userList.forEach(item -> {
            ChatUserResult chatUserResult = new ChatUserResult();
            BeanUtil.copyProperties(item, chatUserResult);
            list.add(chatUserResult);
        });
        ChatSpaceQuery spaceQuery = new ChatSpaceQuery();
        spaceQuery.setSenderId(SessionHolder.getCurrentUserId());
        spaceQuery.setSpaceType(ChatSpaceTypeEnum.USER.getValue());
        List<ChatSpaceResult> spaceList = chatSpaceDao.queryChatSpaceList(spaceQuery);
        if (CollectionUtil.isEmpty(spaceList)) {
            result.setData(list);
            result.setCode(ResultData.OK);
            return result;
        }
        Map<Long, Long> receiverSpaceIdMap = spaceList.stream().collect(Collectors.toMap(ChatSpaceResult::getReceiverId, ChatSpaceResult::getSpaceId, (v1, v2) -> v2));
        ChatMsgQuery chatMsgQuery = new ChatMsgQuery();
        chatMsgQuery.setReceiverId(userId);
        chatMsgQuery.setReadStatus(ChatMsgStatusEnum.NO.getValue());
        list.forEach(item -> {
            Long spaceId = receiverSpaceIdMap.get(item.getId());
            if (spaceId != null) {
                item.setChatSpaceId(spaceId);
                chatMsgQuery.setSpaceId(spaceId);
                int count = chatMsgDao.queryChatMsgCount(chatMsgQuery);
                item.setNoReadCount(count);
            }
        });
        list.sort(Comparator.comparingInt(ChatUserResult::getNoReadCount).reversed());
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }
}
