package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.Message;
import com.spark.common.bean.sys.query.MessageQuery;
import com.spark.common.bean.sys.result.MessageResult;
import com.spark.dao.BaseDao;

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
public interface MessageDao extends BaseDao<Message> {
    /**
     * 插入数据
     * @param message
     * @return
     */
    @Override
    int insert(Message message);

    /**
     * 删除数据
     * @param message
     * @return
     */
    @Override
    int deleteById(Message message);

    /**
     * 修改数据
     * @param message
     * @return
     */
    @Override
    int updateById(Message message);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryMessageCount(MessageQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<MessageResult> queryMessageList(MessageQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    MessageResult queryMessage(MessageQuery query);

    /**
     * 查询我的消息数量
     * @param query 查询参数（按 userId 关联接收人）
     * @return 数量
     */
    int queryMyMessageCount(MessageQuery query);

    /**
     * 查询我的消息列表
     * @param query 查询参数（按 userId 关联接收人）
     * @return 列表
     */
    List<MessageResult> queryMyMessageList(MessageQuery query);
}
