package com.spark.web.rest.controller.chat;

import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.result.ChatUserResult;
import com.spark.chat.service.IChatUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:00
 */
@RestController
@RequestMapping("chat/user")
public class ChatUserController {
    @Autowired
    private IChatUserService chatUserService;

    /**
     * 分页查询我的聊天用户列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageMyList")
    public ResultData<PageResult<ChatUserResult>> pageMyChatUserList(UserQuery query) {
        return chatUserService.pageMyChatUserList(query);
    }

}
