package com.spark.chat.service;

import com.spark.bean.base.ResultData;
import com.spark.bean.chat.entity.ChatSpace;
import com.spark.bean.chat.vo.ChatSpaceVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:37
 */
public interface IChatSpaceService {
    /**
     * 创建聊天空间
     * @param chatSpaceVO
     * @return
     */
    ResultData<ChatSpace> createChatSpace(ChatSpaceVO chatSpaceVO);
}
