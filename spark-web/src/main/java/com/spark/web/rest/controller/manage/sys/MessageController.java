package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.query.MessageQuery;
import com.spark.common.bean.sys.result.MessageCountResult;
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
     * 查询我的消息分类计数，全部、未读、已读，供筛选标签展示数量
     * @return 分类计数
     */
    @GetMapping("myCount")
    public ResultData<MessageCountResult> queryMyMessageCount() {
        return messageService.queryMyMessageCount();
    }

    /**
     * 标记消息已读：点开消息详情时调用，幂等
     * @param messageVO 消息参数，只需 id
     * @return 处理结果
     */
    @PostMapping("read")
    public ResultData<Void> readMessage(MessageVO messageVO) {
        return messageService.readMessage(messageVO);
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
