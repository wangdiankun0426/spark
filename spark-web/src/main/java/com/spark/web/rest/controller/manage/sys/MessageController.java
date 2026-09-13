package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.MessageQuery;
import com.spark.common.bean.sys.result.MessageResult;
import com.spark.common.bean.sys.vo.MessageVO;
import com.spark.manage.sys.IMessageService;
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
 * @since 2024-06-18 16:08:03
 */
@RestController
@RequestMapping("sys/message")
public class MessageController {
    @Autowired
    private IMessageService messageService;

    /**
     * 分页查询我的消息
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("pageMyList")
    public ResultData<PageResult<MessageResult>> pageMyMessageList(MessageQuery query) {
        return messageService.pageMyMessageList(query);
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
