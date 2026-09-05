package com.spark.web.rest.controller.chat;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.entity.ChatSpace;
import com.spark.common.bean.chat.vo.ChatSpaceVO;
import com.spark.chat.service.IChatSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
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
     *
     * @param chatSpaceVO 聊天空间信息
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<ChatSpace> createChatSpace(ChatSpaceVO chatSpaceVO) {
        return chatSpaceService.createChatSpace(chatSpaceVO);
    }
}
