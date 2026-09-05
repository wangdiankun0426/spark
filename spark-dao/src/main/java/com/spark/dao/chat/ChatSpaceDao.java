package com.spark.dao.chat;

import com.spark.common.bean.chat.entity.ChatSpace;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.ChatSpaceResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 19:25
 */
public interface ChatSpaceDao extends BaseDao<ChatSpace> {
    /**
     * 查询聊天空间列表
     * @param spaceQuery
     * @return
     */
    List<ChatSpaceResult> queryChatSpaceList(ChatSpaceQuery spaceQuery);

    /**
     * 查询聊天空间
     * @param spaceQuery
     * @return
     */
    ChatSpaceResult queryChatSpace(ChatSpaceQuery spaceQuery);

    /**
     * 插入数据
     * @param chatSpace
     * @return
     */
    @Override
    int insert(ChatSpace chatSpace);

    /**
     * 查询最大聊天空间id
     * @return
     */
    @Select("select max(space_id) from chat_space")
    Long queryChatSpaceMaxId();
}
