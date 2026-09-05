package com.spark.manage.sys;

import com.spark.common.bean.sys.query.MessageQuery;
import com.spark.common.bean.sys.result.MessageResult;
import com.spark.common.bean.sys.vo.MessageVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

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

    /**
     * 分页查询消息列表
     * @param query 查询参数
     * @return 分页结果
     */
    ResultData<PageResult<MessageResult>> pageMessageList(MessageQuery query);

    /**
     * 发送消息
     * @param messageVO 消息数据
     * @return 发送结果
     */
    ResultData<Void> sendMessage(MessageVO messageVO);

    /**
     * 删除消息
     * @param messageVO 删除参数
     * @return 删除结果
     */
    ResultData<Void> deleteMessage(MessageVO messageVO);
}
