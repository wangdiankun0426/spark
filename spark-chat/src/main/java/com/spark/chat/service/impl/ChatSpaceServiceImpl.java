package com.spark.chat.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.entity.ChatSpace;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.ChatSpaceResult;
import com.spark.common.bean.chat.vo.ChatSpaceVO;
import com.spark.dao.chat.ChatSpaceDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.manage.BaseService;
import com.spark.chat.service.IChatSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:39
 */
@Service
public class ChatSpaceServiceImpl extends BaseService<ChatSpaceQuery, ChatSpaceResult> implements IChatSpaceService {
    @Autowired
    private ChatSpaceDao chatSpaceDao;

    /**
     * 创建聊天空间
     * @param chatSpaceVO 聊天空间信息
     * @return 创建结果
     */
    @Override
    public ResultData<ChatSpace> createChatSpace(ChatSpaceVO chatSpaceVO) {
        ResultData<ChatSpace> result = new ResultData<>();
        if (chatSpaceVO == null || chatSpaceVO.getSenderId() == null || chatSpaceVO.getReceiverId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ChatSpace chatSpace = new ChatSpace();
        ChatSpaceQuery chatSpaceQuery = new ChatSpaceQuery();
        chatSpaceQuery.setSenderId(chatSpaceVO.getSenderId());
        chatSpaceQuery.setReceiverId(chatSpaceVO.getReceiverId());
        ChatSpaceResult chatSpaceResult = chatSpaceDao.queryChatSpace(chatSpaceQuery);
        if (chatSpaceResult != null) {
            chatSpace.setSpaceId(chatSpaceResult.getSpaceId());
            result.setData(chatSpace);
            result.setCode(ResultData.OK);
            return result;
        }
        Long chatSpaceId = super.genObjectId(ObjectTypeEnum.CHAT_SPACE);
        chatSpace.setSpaceId(chatSpaceId);
        chatSpace.setSenderId(chatSpaceVO.getSenderId());
        chatSpace.setReceiverId(chatSpaceVO.getReceiverId());
        int count = chatSpaceDao.insertDB(chatSpace);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        chatSpace.setSenderId(chatSpaceVO.getReceiverId());
        chatSpace.setReceiverId(chatSpaceVO.getSenderId());
        count = chatSpaceDao.insertDB(chatSpace);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setData(chatSpace);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询最大id
     * @return 最大id
     */
    @Override
    protected Long queryMaxId() {
        return chatSpaceDao.queryChatSpaceMaxId();
    }
}
