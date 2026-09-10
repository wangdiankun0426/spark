package com.spark.chat.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.entity.ChatSpace;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.AiChatSpaceResult;
import com.spark.common.bean.chat.vo.ChatSpaceVO;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:37
 */
public interface IChatSpaceService {
    /**
     * 创建聊天空间
     * @param chatSpaceVO
     * @return
     */
    ResultData<ChatSpace> createChatSpace(ChatSpaceVO chatSpaceVO);

    /**
     * 创建AI会话
     * @param chatSpaceVO 会话参数
     * @return 创建结果
     */
    ResultData<ChatSpace> createAiChatSpace(ChatSpaceVO chatSpaceVO);

    /**
     * 查询我的AI会话列表
     * @param query 查询参数
     * @return 会话列表
     */
    ResultData<List<AiChatSpaceResult>> queryMyAiChatSpaceList(ChatSpaceQuery query);

    /**
     * 重命名我的AI会话
     * @param chatSpaceVO 会话参数
     * @return 修改结果
     */
    ResultData<Void> updateAiChatSpaceTitle(ChatSpaceVO chatSpaceVO);

    /**
     * 删除我的AI会话
     * @param chatSpaceVO 会话参数
     * @return 删除结果
     */
    ResultData<Void> deleteAiChatSpace(ChatSpaceVO chatSpaceVO);

    /**
     * 标题为空时回填会话标题
     * @param spaceId 空间id
     * @param content 提问内容
     * @param updatedBy 修改人id
     */
    void fillTitleIfBlank(Long spaceId, String content, Long updatedBy);
}
