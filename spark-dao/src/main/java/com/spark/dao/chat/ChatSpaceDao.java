package com.spark.dao.chat;

import com.spark.common.bean.chat.entity.ChatSpace;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.AiChatSpaceResult;
import com.spark.common.bean.chat.result.ChatSpaceResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
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
     * 查询我的AI会话列表
     * @param spaceQuery 查询参数
     * @return 会话列表
     */
    List<AiChatSpaceResult> queryMyAiChatSpaceList(ChatSpaceQuery spaceQuery);

    /**
     * 插入数据
     * @param chatSpace
     * @return
     */
    @Override
    int insert(ChatSpace chatSpace);

    /**
     * 根据id修改数据
     * @param chatSpace 聊天空间
     * @return 修改条数
     */
    @Override
    int updateById(ChatSpace chatSpace);

    /**
     * 根据id删除数据
     * @param chatSpace 聊天空间
     * @return 删除条数
     */
    @Override
    int deleteById(ChatSpace chatSpace);

    /**
     * 标题为空时回填会话标题
     * @param spaceId 空间id
     * @param title 会话标题
     * @param updatedBy 修改人id
     * @return 修改条数
     */
    int updateTitleBySpaceId(@Param("spaceId") Long spaceId, @Param("title") String title, @Param("updatedBy") Long updatedBy);

    /**
     * 查询最大聊天空间id
     * @return
     */
    @Select("select max(space_id) from chat_space")
    Long queryChatSpaceMaxId();
}
