package com.spark.web.controller.system;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.system.query.MessageQuery;
import com.spark.bean.system.result.MessageResult;
import com.spark.bean.system.vo.MessageVO;
import com.spark.manage.system.IMessageService;
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
 * @since 2024-06-18 16:08:03
 */
@RestController
@RequestMapping("system/message")
public class MessageController {
    @Autowired
    private IMessageService messageService;

    /**
     * 我的查询消息
     * @return 列表
     */
    @GetMapping("myList")
    public ResultData<List<MessageResult>> queryMyMessageList() {
        return messageService.queryMyMessageList();
    }

    /**
     * 分页查询消息列表
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageList")
    private ResultData<PageResult<MessageResult>> pageMessageList(MessageQuery query) {
        return messageService.pageMessageList(query);
    }

    /**
     * 发送消息
     * @param messageVO 消息参数
     * @return 发送结果
     */
    @PostMapping("send")
    private ResultData<Void> sendMessage(MessageVO messageVO) {
        return messageService.sendMessage(messageVO);
    }

    /**
     * 删除消息
     * @param messageVO 删除参数
     * @return 删除结果
     */
    @PostMapping("delete")
    private ResultData<Void> deleteMessage(MessageVO messageVO) {
        return messageService.deleteMessage(messageVO);
    }

}
