package com.spark.web.rest.controller.chat;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.entity.ChatSpace;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.AiChatSpaceResult;
import com.spark.common.bean.chat.vo.ChatSpaceVO;
import com.spark.chat.service.IChatSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
     * 查询我的AI会话列表
     * @param query 查询参数
     * @return 会话列表
     */
    @GetMapping("aiMyList")
    public ResultData<List<AiChatSpaceResult>> queryMyAiChatSpaceList(ChatSpaceQuery query) {
        return chatSpaceService.queryMyAiChatSpaceList(query);
    }

    /**
     * 重命名我的AI会话
     * @param chatSpaceVO 会话参数
     * @return 修改结果
     */
    @PostMapping("aiRename")
    public ResultData<Void> updateAiChatSpaceTitle(ChatSpaceVO chatSpaceVO) {
        return chatSpaceService.updateAiChatSpaceTitle(chatSpaceVO);
    }

    /**
     * 删除我的AI会话
     * @param chatSpaceVO 会话参数
     * @return 删除结果
     */
    @PostMapping("aiDelete")
    public ResultData<Void> deleteAiChatSpace(ChatSpaceVO chatSpaceVO) {
        return chatSpaceService.deleteAiChatSpace(chatSpaceVO);
    }
}
