package com.spark.web.controller.system;

import com.spark.bean.base.ResultData;
import com.spark.bean.system.result.MessageResult;
import com.spark.manage.system.IMessageService;
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

}
