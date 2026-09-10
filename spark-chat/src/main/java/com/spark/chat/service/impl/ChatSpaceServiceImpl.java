package com.spark.chat.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.chat.entity.ChatSpace;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.AiChatSpaceResult;
import com.spark.common.bean.chat.result.ChatSpaceResult;
import com.spark.common.bean.chat.vo.ChatSpaceVO;
import com.spark.common.bean.llm.query.AgentQuery;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.AgentResult;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.enums.ChatSpaceTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ModelTypeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.dao.chat.ChatMsgDao;
import com.spark.dao.chat.ChatSpaceDao;
import com.spark.dao.llm.AgentDao;
import com.spark.dao.llm.ModelDao;
import com.spark.manage.BaseService;
import com.spark.chat.service.IChatSpaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/17 20:39
 */
@Service
public class ChatSpaceServiceImpl extends BaseService<ChatSpaceQuery, ChatSpaceResult> implements IChatSpaceService {
    private final static Logger logger = LoggerFactory.getLogger(ChatSpaceServiceImpl.class);
    private final static int TITLE_MAX_LENGTH = 50;
    private final static int LAST_MESSAGE_MAX_LENGTH = 50;
    @Autowired
    private ChatSpaceDao chatSpaceDao;
    @Autowired
    private ChatMsgDao chatMsgDao;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private AgentDao agentDao;

    /**
     * 创建聊天空间
     * @param chatSpaceVO 聊天空间信息
     * @return 创建结果
     */
    @Override
    public ResultData<ChatSpace> createChatSpace(ChatSpaceVO chatSpaceVO) {
        ResultData<ChatSpace> result = new ResultData<>();
        if (chatSpaceVO == null || chatSpaceVO.getSenderId() == null || chatSpaceVO.getReceiverId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ChatSpace chatSpace = new ChatSpace();
        ChatSpaceQuery chatSpaceQuery = new ChatSpaceQuery();
        chatSpaceQuery.setSenderId(chatSpaceVO.getSenderId());
        chatSpaceQuery.setReceiverId(chatSpaceVO.getReceiverId());
        ChatSpaceResult chatSpaceResult = chatSpaceDao.queryChatSpace(chatSpaceQuery);
        if (chatSpaceResult != null) {
            chatSpace.setSpaceId(chatSpaceResult.getSpaceId());
            result.setData(chatSpace);
            result.setCode(ResultData.OK);
            return result;
        }
        Long chatSpaceId = super.genObjectId(ObjectTypeEnum.CHAT_SPACE);
        chatSpace.setSpaceId(chatSpaceId);
        chatSpace.setSenderId(chatSpaceVO.getSenderId());
        chatSpace.setReceiverId(chatSpaceVO.getReceiverId());
        chatSpace.setSpaceType(ChatSpaceTypeEnum.USER.getValue());
        int count = chatSpaceDao.insertDB(chatSpace);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        chatSpace.setSenderId(chatSpaceVO.getReceiverId());
        chatSpace.setReceiverId(chatSpaceVO.getSenderId());
        count = chatSpaceDao.insertDB(chatSpace);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setData(chatSpace);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 创建AI会话
     * 与用户对话不同，AI会话不幂等且仅插入用户到模型一行，模型不参与会话检索
     * @param chatSpaceVO 会话参数
     * @return 创建结果
     */
    @Override
    public ResultData<ChatSpace> createAiChatSpace(ChatSpaceVO chatSpaceVO) {
        ResultData<ChatSpace> result = new ResultData<>();
        if (chatSpaceVO == null || chatSpaceVO.getReceiverId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        Long receiverId = chatSpaceVO.getReceiverId();
        ObjectTypeEnum receiverType = super.getObjEnum(receiverId);
        Integer spaceType;
        if (ObjectTypeEnum.MODEL.equals(receiverType)) {
            ModelResult modelResult = this.queryValidModel(receiverId);
            if (modelResult == null) {
                result.setErrorCode(ErrorCodeEnum.MODEL_NOT_EXIST);
                return result;
            }
            spaceType = ChatSpaceTypeEnum.MODEL.getValue();
        } else if (ObjectTypeEnum.AGENT.equals(receiverType)) {
            AgentResult agentResult = this.queryValidAgent(receiverId);
            if (agentResult == null) {
                result.setErrorCode(ErrorCodeEnum.AGENT_NOT_EXIST);
                return result;
            }
            spaceType = ChatSpaceTypeEnum.AGENT.getValue();
        } else {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long chatSpaceId = super.genObjectId(ObjectTypeEnum.CHAT_SPACE);
        ChatSpace chatSpace = new ChatSpace();
        chatSpace.setSpaceId(chatSpaceId);
        chatSpace.setSenderId(userId);
        chatSpace.setReceiverId(receiverId);
        chatSpace.setSpaceType(spaceType);
        int count = chatSpaceDao.insertDB(chatSpace);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setData(chatSpace);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询我的AI会话列表
     * @param query 查询参数
     * @return 会话列表
     */
    @Override
    public ResultData<List<AiChatSpaceResult>> queryMyAiChatSpaceList(ChatSpaceQuery query) {
        ResultData<List<AiChatSpaceResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (query == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        boolean isAiSpace = this.isAiSpaceType(query.getSpaceType());
        if (!isAiSpace) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        query.setSenderId(userId);
        List<AiChatSpaceResult> list = chatSpaceDao.queryMyAiChatSpaceList(query);
        this.supplyAiChatSpaceList(list);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 重命名我的AI会话
     * @param chatSpaceVO 会话参数
     * @return 修改结果
     */
    @Override
    public ResultData<Void> updateAiChatSpaceTitle(ChatSpaceVO chatSpaceVO) {
        ResultData<Void> result = new ResultData<>();
        if (chatSpaceVO == null || chatSpaceVO.getSpaceId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(chatSpaceVO.getTitle())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        ChatSpaceResult chatSpaceResult = this.queryMyAiChatSpace(chatSpaceVO.getSpaceId(), userId);
        if (chatSpaceResult == null) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        String title = this.cutOut(chatSpaceVO.getTitle(), TITLE_MAX_LENGTH);
        ChatSpace chatSpace = new ChatSpace();
        chatSpace.setId(chatSpaceResult.getId());
        chatSpace.setTitle(title);
        int count = chatSpaceDao.updateDBById(chatSpace);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除我的AI会话
     * @param chatSpaceVO 会话参数
     * @return 删除结果
     */
    @Override
    public ResultData<Void> deleteAiChatSpace(ChatSpaceVO chatSpaceVO) {
        ResultData<Void> result = new ResultData<>();
        if (chatSpaceVO == null || chatSpaceVO.getSpaceId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        ChatSpaceResult chatSpaceResult = this.queryMyAiChatSpace(chatSpaceVO.getSpaceId(), userId);
        if (chatSpaceResult == null) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        ChatSpace chatSpace = new ChatSpace();
        chatSpace.setId(chatSpaceResult.getId());
        int count = chatSpaceDao.deleteDBById(chatSpace);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        chatMsgDao.deleteBySpaceId(chatSpaceVO.getSpaceId(), userId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 标题为空时回填会话标题
     * @param spaceId 空间id
     * @param content 提问内容
     * @param updatedBy 修改人id
     */
    @Override
    public void fillTitleIfBlank(Long spaceId, String content, Long updatedBy) {
        if (spaceId == null || StringUtil.isBlank(content)) {
            logger.warn("fillTitleIfBlank skip, invalid param, spaceId={}", spaceId);
            return;
        }
        try {
            String title = this.buildTitle(content);
            chatSpaceDao.updateTitleBySpaceId(spaceId, title, updatedBy);
        } catch (Exception e) {
            logger.error("fillTitleIfBlank error, spaceId={}", spaceId, e);
        }
    }

    /**
     * 查询我的AI会话，会话不存在或不属于当前用户时返回空
     * @param spaceId 空间id
     * @param userId 用户id
     * @return 会话
     */
    private ChatSpaceResult queryMyAiChatSpace(Long spaceId, Long userId) {
        ChatSpaceQuery spaceQuery = new ChatSpaceQuery();
        spaceQuery.setSpaceId(spaceId);
        ChatSpaceResult chatSpaceResult = chatSpaceDao.queryChatSpace(spaceQuery);
        if (chatSpaceResult == null) {
            return null;
        }
        if (!userId.equals(chatSpaceResult.getSenderId())) {
            return null;
        }
        boolean isAiSpace = this.isAiSpaceType(chatSpaceResult.getSpaceType());
        if (!isAiSpace) {
            return null;
        }
        return chatSpaceResult;
    }

    /**
     * 判断是否为AI会话类型
     * @param spaceType 会话类型
     * @return 是否AI会话
     */
    private boolean isAiSpaceType(Integer spaceType) {
        if (ChatSpaceTypeEnum.MODEL.getValue().equals(spaceType)) {
            return true;
        }
        return ChatSpaceTypeEnum.AGENT.getValue().equals(spaceType);
    }

    /**
     * 补充AI会话列表数据
     * @param list 会话列表
     */
    private void supplyAiChatSpaceList(List<AiChatSpaceResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        Map<Long, String> receiverNameMap = this.queryReceiverNameMap(list);
        list.forEach(item -> {
            item.setModelName(receiverNameMap.get(item.getReceiverId()));
            String lastMessage = item.getLastMessage();
            if (StringUtil.isNotBlank(lastMessage)) {
                item.setLastMessage(this.cutOut(lastMessage, LAST_MESSAGE_MAX_LENGTH));
            }
        });
        Comparator<AiChatSpaceResult> comparator = Comparator.comparing(AiChatSpaceResult::getLastTime,
                Comparator.nullsLast(Comparator.reverseOrder()));
        list.sort(comparator);
    }

    /**
     * 查询会话接收对象的名称，语言模型与智能体分别取名
     * @param list 会话列表
     * @return 接收对象id与名称的映射
     */
    private Map<Long, String> queryReceiverNameMap(List<AiChatSpaceResult> list) {
        Map<Long, String> nameMap = new HashMap<>();
        List<Long> modelIds = new ArrayList<>();
        List<Long> agentIds = new ArrayList<>();
        for (AiChatSpaceResult item : list) {
            Long receiverId = item.getReceiverId();
            if (receiverId == null) {
                continue;
            }
            ObjectTypeEnum receiverType = super.getObjEnum(receiverId);
            if (ObjectTypeEnum.MODEL.equals(receiverType)) {
                modelIds.add(receiverId);
            } else if (ObjectTypeEnum.AGENT.equals(receiverType)) {
                agentIds.add(receiverId);
            }
        }
        if (CollectionUtil.isNotEmpty(modelIds)) {
            ModelQuery modelQuery = new ModelQuery();
            modelQuery.setIds(modelIds);
            modelQuery.setPage(false);
            List<ModelResult> modelList = modelDao.queryModelList(modelQuery);
            modelList.forEach(model -> nameMap.put(model.getId(), model.getName()));
        }
        if (CollectionUtil.isNotEmpty(agentIds)) {
            AgentQuery agentQuery = new AgentQuery();
            agentQuery.setIds(agentIds);
            agentQuery.setPage(false);
            List<AgentResult> agentList = agentDao.queryAgentList(agentQuery);
            agentList.forEach(agent -> nameMap.put(agent.getId(), agent.getName()));
        }
        return nameMap;
    }

    /**
     * 查询可用的语言模型，不存在或未启用时返回空
     * @param modelId 模型id
     * @return 模型
     */
    private ModelResult queryValidModel(Long modelId) {
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setId(modelId);
        ModelResult modelResult = modelDao.queryModel(modelQuery);
        if (modelResult == null) {
            return null;
        }
        boolean isLanguage = ModelTypeEnum.LANGUAGE.getType().equals(modelResult.getType());
        if (!isLanguage) {
            return null;
        }
        boolean isNormal = StatusEnum.NORMAL.getValue().equals(modelResult.getStatus());
        if (!isNormal) {
            return null;
        }
        return modelResult;
    }

    /**
     * 查询可用的智能体，不存在或未启用时返回空
     * @param agentId 智能体id
     * @return 智能体
     */
    private AgentResult queryValidAgent(Long agentId) {
        AgentQuery agentQuery = new AgentQuery();
        agentQuery.setId(agentId);
        AgentResult agentResult = agentDao.queryAgent(agentQuery);
        if (agentResult == null) {
            return null;
        }
        boolean isNormal = StatusEnum.NORMAL.getValue().equals(agentResult.getStatus());
        if (!isNormal) {
            return null;
        }
        return agentResult;
    }

    /**
     * 由提问内容生成会话标题，取首行并截断
     * @param content 提问内容
     * @return 会话标题
     */
    private String buildTitle(String content) {
        String title = content.trim();
        int lineIndex = title.indexOf("\n");
        if (lineIndex > 0) {
            title = title.substring(0, lineIndex).trim();
        }
        return this.cutOut(title, TITLE_MAX_LENGTH);
    }

    /**
     * 截断文本
     * @param text 原文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    private String cutOut(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    /**
     * 查询最大id
     * @return 最大id
     */
    @Override
    protected Long queryMaxId() {
        return chatSpaceDao.queryChatSpaceMaxId();
    }
}
