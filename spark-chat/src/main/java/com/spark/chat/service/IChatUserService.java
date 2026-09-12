package com.spark.chat.service;

import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.result.ChatUserResult;

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
     * 分页查询我的聊天用户列表
     * @param query 查询参数
     * @return 分页结果
     */
    ResultData<PageResult<ChatUserResult>> pageMyChatUserList(UserQuery query);
}
