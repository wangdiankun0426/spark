package com.spark.web.rest.controller.chat;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.entity.ChatSpace;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.ChatSpaceResult;
import com.spark.common.bean.chat.vo.ChatSpaceVO;
import com.spark.chat.service.IChatSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:36
 */
@RestController
@RequestMapping("chat/space")
public class ChatSpaceController {

    @Autowired
    private IChatSpaceService chatSpaceService;

    /**
     * 创建聊天空间
     * @param chatSpaceVO 聊天空间信息
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<ChatSpace> createChatSpace(ChatSpaceVO chatSpaceVO) {
        return chatSpaceService.createChatSpace(chatSpaceVO);
    }

    /**
     * 创建AI会话
     * @param chatSpaceVO 会话参数
     * @return 创建结果
     */
    @PostMapping("aiCreate")
    public ResultData<ChatSpace> createAiChatSpace(ChatSpaceVO chatSpaceVO) {
        return chatSpaceService.createAiChatSpace(chatSpaceVO);
    }

    /**
     * 分页查询我的会话列表
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageMyList")
    public ResultData<PageResult<ChatSpaceResult>> pageMyChatSpaceList(ChatSpaceQuery query) {
        return chatSpaceService.pageMyChatSpaceList(query);
    }

    /**
     * 重命名会话
     * @param chatSpaceVO 会话参数
     * @return 修改结果
     */
    @PostMapping("rename")
    public ResultData<Void> updateChatSpaceTitle(ChatSpaceVO chatSpaceVO) {
        return chatSpaceService.updateChatSpaceTitle(chatSpaceVO);
    }

    /**
     * 删除会话
     * @param chatSpaceVO 会话参数
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteChatSpace(ChatSpaceVO chatSpaceVO) {
        return chatSpaceService.deleteChatSpace(chatSpaceVO);
    }
}
