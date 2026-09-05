package com.spark.chat.service;

import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.result.ChatUserResult;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:01
 */
public interface IChatUserService {

    /**
     * 查询我的聊天用户列表
     * @param query
     * @return
     */
    ResultData<List<ChatUserResult>> queryMyChatUserList(UserQuery query);
}
