package com.spark.chat.service.impl;

import com.spark.config.aspectj.annotation.DataScope;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.base.PageResult;
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
import com.spark.manage.BaseService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
 * @since 2024/4/17 20:01
 */
@Service
public class ChatUserServiceImpl extends BaseService<UserQuery, ChatUserResult> implements IChatUserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private ChatSpaceDao chatSpaceDao;
    @Autowired
    private ChatMsgDao chatMsgDao;

    /**
     * 分页查询我的聊天用户列表
     * 先按租户与数据权限分页查用户，再补充会话空间id与未读数
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<ChatUserResult>> pageMyChatUserList(UserQuery query) {
        ResultData<PageResult<ChatUserResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (query == null) {
            query = new UserQuery();
        }
        query.setTenantId(SessionHolder.getCurrentTenantId());
        PageResult<ChatUserResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(UserQuery query) {
        return userDao.queryUserCount(query);
    }

    /**
     * 查询列表，用户结果转聊天用户结果
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<ChatUserResult> queryList(UserQuery query) {
        List<ChatUserResult> list = new ArrayList<>();
        List<UserResult> userList = userDao.queryUserList(query);
        if (CollectionUtil.isEmpty(userList)) {
            return list;
        }
        userList.forEach(item -> {
            ChatUserResult chatUserResult = new ChatUserResult();
            BeanUtil.copyProperties(item, chatUserResult);
            list.add(chatUserResult);
        });
        return list;
    }

    /**
     * 补充直属部门名称、会话空间id与未读消息数
     * @param list 列表
     */
    @Override
    protected void supplyList(List<ChatUserResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        list.forEach(item -> {
            if (item.getDeptId() != null && item.getDeptId() != 0) {
                item.setDeptName(super.getObjName(item.getDeptId()));
            }
        });
        Long userId = SessionHolder.getCurrentUserId();
        ChatSpaceQuery spaceQuery = new ChatSpaceQuery();
        spaceQuery.setSenderId(userId);
        spaceQuery.setSpaceType(ChatSpaceTypeEnum.USER.getValue());
        List<ChatSpaceResult> spaceList = chatSpaceDao.queryChatSpaceList(spaceQuery);
        if (CollectionUtil.isEmpty(spaceList)) {
            return;
        }
        Map<Long, Long> receiverSpaceIdMap = spaceList.stream()
                .collect(Collectors.toMap(ChatSpaceResult::getReceiverId, ChatSpaceResult::getSpaceId, (v1, v2) -> v2));
        ChatMsgQuery chatMsgQuery = new ChatMsgQuery();
        chatMsgQuery.setReceiverId(userId);
        chatMsgQuery.setReadStatus(ChatMsgStatusEnum.NO.getValue());
        list.forEach(item -> {
            Long spaceId = receiverSpaceIdMap.get(item.getId());
            if (spaceId == null) {
                return;
            }
            item.setChatSpaceId(spaceId);
            chatMsgQuery.setSpaceId(spaceId);
            int count = chatMsgDao.queryChatMsgCount(chatMsgQuery);
            item.setNoReadCount(count);
        });
    }
}
