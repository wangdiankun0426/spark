package com.spark.web.controller.chat;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.chat.query.ChatMsgQuery;
import com.spark.bean.chat.result.ChatMsgResult;
import com.spark.chat.service.IChatMsgService;
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
 * @since 2024/4/17 20:53
 */
@RestController
@RequestMapping("chat/msg")
public class ChatMsgController {

    @Autowired
    private IChatMsgService chatMsgService;

    /**
     * 查询未读消息列表
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("noReadList")
    public ResultData<List<ChatMsgResult>> queryNoReadChatMsgList(ChatMsgQuery query) {
        return chatMsgService.queryNoReadChatMsgList(query);
    }

    /**
     * 查询聊天空间消息列表
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("list")
    public ResultData<List<ChatMsgResult>> queryChatSpaceMsgList(ChatMsgQuery query) {
        return chatMsgService.queryChatSpaceMsgList(query);
    }

    /**
     * 分页查询我的消息列表
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageMyList")
    public ResultData<PageResult<ChatMsgResult>> pageMyChatMsgList(ChatMsgQuery query) {
        return chatMsgService.pageMyChatMsgList(query);
    }

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("pageList")
    public ResultData<PageResult<ChatMsgResult>> pageChatMsgList(ChatMsgQuery query) {
        return chatMsgService.pageChatMsgList(query);
    }
}
