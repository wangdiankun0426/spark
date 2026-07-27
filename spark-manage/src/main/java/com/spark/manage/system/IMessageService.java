package com.spark.manage.system;

import com.spark.bean.system.result.MessageResult;
import com.spark.bean.system.vo.MessageVO;
import com.spark.bean.base.ResultData;

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
public interface IMessageService {

    /**
     * 创建消息
     * @param messageVO 消息数据
     * @return 创建结果
     */
    ResultData<Void> createMessage(MessageVO messageVO);

    /**
     * 分页我的消息列表
     * @return 列表
     */
    ResultData<List<MessageResult>> queryMyMessageList();
}
