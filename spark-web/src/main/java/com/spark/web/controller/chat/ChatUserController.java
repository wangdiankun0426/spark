package com.spark.web.controller.chat;

import com.spark.bean.system.query.UserQuery;
import com.spark.bean.base.ResultData;
import com.spark.bean.chat.result.ChatUserResult;
import com.spark.chat.service.IChatUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 查询我的聊天用户列表
     *
     * @param query 查询参数
     * @return 结果
     */
    @GetMapping("myList")
    public ResultData<List<ChatUserResult>> queryMyChatUserList(UserQuery query) {
        return chatUserService.queryMyChatUserList(query);
    }

}
