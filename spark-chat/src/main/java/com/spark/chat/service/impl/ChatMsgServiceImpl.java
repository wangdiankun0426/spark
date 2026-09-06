package com.spark.chat.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.chat.entity.ChatMsg;
import com.spark.common.bean.chat.entity.ChatMsgAtt;
import com.spark.common.bean.chat.query.ChatMsgAttQuery;
import com.spark.common.bean.chat.query.ChatMsgQuery;
import com.spark.common.bean.chat.query.ChatSpaceQuery;
import com.spark.common.bean.chat.result.ChatMsgAttResult;
import com.spark.common.bean.chat.result.ChatMsgResult;
import com.spark.common.bean.chat.result.ChatSpaceResult;
import com.spark.common.bean.chat.vo.ChatMsgVO;
import com.spark.common.bean.chat.vo.DataContentVO;
import com.spark.common.bean.llm.query.AgentQuery;
import com.spark.common.bean.llm.query.ModelQuery;
import com.spark.common.bean.llm.result.AgentResult;
import com.spark.common.bean.llm.result.ModelResult;
import com.spark.common.enums.*;
import com.spark.dao.llm.ModelDao;
import com.spark.llm.IAgent;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.tools.SearchKnowledge;
import com.spark.dao.chat.ChatMsgAttDao;
import com.spark.dao.chat.ChatMsgDao;
import com.spark.dao.chat.ChatSpaceDao;
import com.spark.dao.llm.AgentDao;
import com.spark.llm.model.ModelFactory;
import com.spark.manage.BaseService;
import com.spark.chat.service.IChatMsgService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/14 21:58
 */
@Service
public class ChatMsgServiceImpl extends BaseService<ChatMsgQuery, ChatMsgResult> implements IChatMsgService {
    private final static Logger logger = LoggerFactory.getLogger(ChatMsgServiceImpl.class);
    @Autowired
    private ChatSpaceDao chatSpaceDao;
    @Autowired
    private ChatMsgDao chatMsgDao;
    @Autowired
    private AgentFactory agentFactory;
    @Autowired
    private AgentDao agentDao;
    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private ModelDao modelDao;
    @Autowired
    private SearchKnowledge searchKnowledge;
    @Autowired
    private ChatMsgAttDao chatMsgAttDao;

    /**
     * 创建聊天消息
     * @param chatMsgVO 聊天消息
     * @return 创建结果
     */
    @Override
    public ResultData<ChatMsg> createChatMsg(ChatMsgVO chatMsgVO) {
        ResultData<ChatMsg> result = new ResultData<>();
        if (chatMsgVO == null || chatMsgVO.getSpaceId() == null || chatMsgVO.getSenderId() == null
                || chatMsgVO.getReceiverId() == null || StringUtil.isBlank(chatMsgVO.getMessage())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setSpaceId(chatMsgVO.getSpaceId());
        chatMsg.setSenderId(chatMsgVO.getSenderId());
        chatMsg.setReceiverId(chatMsgVO.getReceiverId());
        chatMsg.setMessage(chatMsgVO.getMessage());
        chatMsg.setCreatedBy(chatMsgVO.getSenderId());
        chatMsg.setUpdatedBy(chatMsgVO.getSenderId());
        ObjectTypeEnum objEnum = super.getObjEnum(chatMsgVO.getReceiverId());
        // 如果发送人和接收人是同一人/接收人是agent 则直接标记位已签收已读
        if (chatMsgVO.getSenderId().equals(chatMsgVO.getReceiverId()) || ObjectTypeEnum.AGENT.equals(objEnum)) {
            chatMsg.setSignStatus(ChatMsgStatusEnum.OK.getValue());
            chatMsg.setReadStatus(ChatMsgStatusEnum.OK.getValue());
        }
        int count = chatMsgDao.insertDB(chatMsg);
        if (count < 1) {
            return result;
        }
        result.setData(chatMsg);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 创建agent消息
     * 通过TokenStream实现流式对话，支持thinking/工具调用/结果/回答的分阶段推送
     * @param chatMsgVO 聊天消息
     * @param findSession WebSocket会话
     */
    @Override
    public void createAgentChatMsg(ChatMsgVO chatMsgVO, Session findSession) {
        if (chatMsgVO == null || chatMsgVO.getSpaceId() == null || chatMsgVO.getSenderId() == null
                || chatMsgVO.getReceiverId() == null || StringUtil.isBlank(chatMsgVO.getMessage())) {
            logger.error("chatHandler create fail, params no full");
            return;
        }
        AgentQuery agentQuery = new AgentQuery();
        agentQuery.setId(chatMsgVO.getReceiverId());
        AgentResult agentResult = agentDao.queryAgent(agentQuery);
        if (agentResult == null) {
            logger.error("chatHandler create fail, agentResult no exist");
            return;
        }
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setSpaceId(chatMsgVO.getSpaceId());
        chatMsg.setSenderId(chatMsgVO.getReceiverId());
        chatMsg.setReceiverId(chatMsgVO.getSenderId());
        chatMsg.setModelId(agentResult.getChatModelId());
        chatMsg.setCreatedBy(chatMsgVO.getReceiverId());
        chatMsg.setUpdatedBy(chatMsgVO.getReceiverId());
        chatMsg.setCreatedDt(new Timestamp(System.currentTimeMillis()));
        chatMsg.setUpdatedDt(new Timestamp(System.currentTimeMillis()));
        // 立即发送thinking状态
        this.sendThinking(findSession, chatMsg, "正在思考中...\n");
        StringBuilder fullAnswer = new StringBuilder();
        IAgent agent = agentFactory.build(chatMsgVO.getReceiverId());
        String question = chatMsgVO.getMessage();
        agent.streamChat(chatMsgVO.getSpaceId(), question)
            .beforeToolExecution(beforeTool -> {
                // 工具调用前：通知前端正在调用哪个工具
                String toolName = beforeTool.request().name();
                this.sendThinking(findSession, chatMsg, "正在调用 " + AgentToolEnum.indexOf(toolName).getDesc() + " 工具...\n");
            })
            .onToolExecuted(toolExecution -> {
                // 工具调用完成：通知前端工具返回结果
                String toolName = toolExecution.request().name();
                String toolResult = toolExecution.result();
                // 截取工具结果前200字符，避免过长
                String briefResult = toolResult != null && toolResult.length() > 200
                        ? toolResult.substring(0, 200) + "..."
                        : toolResult;
                this.sendThinking(findSession, chatMsg, "工具 " + AgentToolEnum.indexOf(toolName).getDesc() + " 返回结果：" + briefResult+"\n");
            })
            .onPartialResponse(response -> {
                // 响应中 - 逐块发送并累积
                fullAnswer.append(response);
                chatMsg.setMessage(response);
                DataContentVO dataContent = new DataContentVO();
                dataContent.setChatMsg(chatMsg);
                this.sendToSession(findSession, dataContent);
            })
            .onCompleteResponse(completeResponse -> {
                // 响应完成 - 保存完整消息到数据库
                String answer = fullAnswer.toString();
                chatMsg.setMessage(answer);
                int count = chatMsgDao.insertDB(chatMsg);
                if (count > 0) {
                    // 保存RAG参考文档
                    List<ChatMsgAttResult> attList = searchKnowledge.getReferences(chatMsgVO.getSpaceId());
                    if (CollectionUtil.isNotEmpty(attList)) {
                        this.saveChatMsgAttList(chatMsg.getId(), attList, chatMsgVO.getSenderId());
                    }
                    chatMsg.setMessage(answer);
                    DataContentVO dataContent = new DataContentVO();
                    dataContent.setChatMsg(chatMsg);
                    if (CollectionUtil.isNotEmpty(attList)) {
                        dataContent.setReferences(attList);
                    }
                    this.sendToSession(findSession, dataContent);
                }
                searchKnowledge.clearReferences(chatMsgVO.getSpaceId());
                logger.info("llm chat completed for spaceId: {}", chatMsgVO.getSpaceId());
            })
            .onError(throwable -> {
                // 异常处理
                String answer = "抱歉，当前Agent对话异常！";
                chatMsg.setMessage(answer);
                int count = chatMsgDao.insertDB(chatMsg);
                if (count > 0) {
                    DataContentVO dataContent = new DataContentVO();
                    dataContent.setChatMsg(chatMsg);
                    this.sendToSession(findSession, dataContent);
                }
                searchKnowledge.clearReferences(chatMsgVO.getSpaceId());
                logger.error("llm chat error", throwable);
            })
            .start();
    }

    /**
     * 创建model消息
     * @param chatMsgVO 聊天消息
     * @param findSession WebSocket会话
     */
    @Override
    public void createModelChatMsg(ChatMsgVO chatMsgVO, Session findSession) {
        if (chatMsgVO == null || chatMsgVO.getSpaceId() == null || chatMsgVO.getSenderId() == null
                || chatMsgVO.getReceiverId() == null || StringUtil.isBlank(chatMsgVO.getMessage())) {
            logger.error("chatHandler create fail, params no full");
            return;
        }
        ModelQuery modelQuery = new ModelQuery();
        modelQuery.setId(chatMsgVO.getReceiverId());
        ModelResult modelResult = modelDao.queryModel(modelQuery);
        if (modelResult == null) {
            logger.error("chatHandler create fail, modelResult no exist");
            return;
        }
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setSpaceId(chatMsgVO.getSpaceId());
        chatMsg.setSenderId(chatMsgVO.getReceiverId());
        chatMsg.setReceiverId(chatMsgVO.getSenderId());
        chatMsg.setCreatedBy(chatMsgVO.getReceiverId());
        chatMsg.setUpdatedBy(chatMsgVO.getReceiverId());

        StringBuilder fullAnswer = new StringBuilder();
        StreamingChatModel streamingChatModel = modelFactory.getStreamingChatModel(modelResult.getId());
        String question = chatMsgVO.getMessage();
        List<ChatMessage> messages = List.of(UserMessage.from(question));
        Flux.<String> create(sink -> {
            try {
                streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        sink.next(partialResponse);
                    }
                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        sink.complete();
                    }
                    @Override
                    public void onError(Throwable error) {
                        sink.error(error);
                    }
                });
            } catch (Exception e) {
                sink.error(e);
            }
        }).doOnNext(response -> {
            // 响应中 - 逐块发送并累积
            fullAnswer.append(response);
            chatMsg.setMessage(response);
            DataContentVO dataContent = new DataContentVO();
            dataContent.setChatMsg(chatMsg);
            this.sendToSession(findSession, dataContent);
        }).doOnComplete(() -> {
            // 响应完成 - 保存完整消息到数据库
            String answer = fullAnswer.toString();
            chatMsg.setMessage(answer);
            int count = chatMsgDao.insertDB(chatMsg);
            if (count > 0) {
                // 发送完成标记
                chatMsg.setMessage(answer);
                DataContentVO dataContent = new DataContentVO();
                dataContent.setChatMsg(chatMsg);
                this.sendToSession(findSession, dataContent);
            }
            logger.info("model chat completed for spaceId: {}", chatMsgVO.getSpaceId());
        }).doOnError(throwable -> {
            // 异常处理
            String answer = "抱歉，当前模型对话异常！";
            chatMsg.setMessage(answer);
            int count = chatMsgDao.insertDB(chatMsg);
            if (count > 0) {
                DataContentVO dataContent = new DataContentVO();
                dataContent.setChatMsg(chatMsg);
                this.sendToSession(findSession, dataContent);
            }
            logger.error("llm chat error", throwable);
        }).subscribe();
    }

    /**
     * 通过WebSocket同步发送消息到客户端
     * @param session WebSocket会话
     * @param dataContent 消息内容
     */
    private void sendToSession(Session session, DataContentVO dataContent) {
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(JsonUtil.toString(dataContent));
            } catch (Exception e) {
                logger.error("send message error", e);
            }
        }
    }

    /**
     * 发送thinking状态消息到客户端
     * @param session WebSocket会话
     * @param chatMsg 聊天消息
     * @param thinkingText 思考状态文本
     */
    private void sendThinking(Session session, ChatMsg chatMsg, String thinkingText) {
        if (session == null || !session.isOpen()) {
            return;
        }
        chatMsg.setMessage(thinkingText);
        DataContentVO dataContent = new DataContentVO();
        dataContent.setAction(MsgActionEnum.THINKING.getValue());
        dataContent.setChatMsg(chatMsg);
        this.sendToSession(session, dataContent);
    }

    /**
     * 签收聊天消息
     * @param chatMsgVO 聊天消息
     * @return 结果
     */
    @Override
    public ResultData<Void> signChatMsg(ChatMsgVO chatMsgVO) {
        ResultData<Void> result = new ResultData<>();
        if (chatMsgVO == null || chatMsgVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setId(chatMsgVO.getId());
        chatMsg.setSignStatus(ChatMsgStatusEnum.OK.getValue());
        int count = chatMsgDao.updateDBById(chatMsg);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 阅读聊天消息
     * @param chatMsgVO 聊天消息
     * @return 结果
     */
    @Override
    public ResultData<Void> readChatMsg(ChatMsgVO chatMsgVO) {
        ResultData<Void> result = new ResultData<>();
        if (chatMsgVO == null || chatMsgVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setId(chatMsgVO.getId());
        chatMsg.setReadStatus(ChatMsgStatusEnum.OK.getValue());
        int count = chatMsgDao.updateDBById(chatMsg);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询聊天空间消息列表
     * @param query 查询参数
     * @return 结果
     */
    @Override
    public ResultData<List<ChatMsgResult>> queryChatSpaceMsgList(ChatMsgQuery query) {
        ResultData<List<ChatMsgResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (query == null || query.getSpaceId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ChatSpaceQuery chatSpaceQuery = new ChatSpaceQuery();
        chatSpaceQuery.setSpaceId(query.getSpaceId());
        ChatSpaceResult chatSpaceResult = chatSpaceDao.queryChatSpace(chatSpaceQuery);
        if (chatSpaceResult == null) {
            result.setErrorCode(ErrorCodeEnum.CHAT_SPACE_NOT_EXIST);
            return result;
        }
        Long receiverId = chatSpaceResult.getReceiverId();
        Long senderId = chatSpaceResult.getSenderId();
        if (!receiverId.equals(userId) && !senderId.equals(userId)) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        query.setPage(false);
        List<ChatMsgResult> chatMsgList = chatMsgDao.queryChatMsgList(query);
        this.supplyList(chatMsgList);
        result.setData(chatMsgList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询未读消息列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    public ResultData<List<ChatMsgResult>> queryNoReadChatMsgList(ChatMsgQuery query) {
        ResultData<List<ChatMsgResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (query == null || query.getSpaceId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        query.setReceiverId(userId);
        query.setReadStatus(ChatMsgStatusEnum.NO.getValue());
        query.setPage(false);
        List<ChatMsgResult> chatMsgList = chatMsgDao.queryChatMsgList(query);
        if (CollectionUtil.isNotEmpty(chatMsgList)) {
            List<Long> ids = chatMsgList.stream().map(ChatMsgResult::getId).toList();
            ChatMsg chatMsg = new ChatMsg();
            chatMsg.setReadStatus(ChatMsgStatusEnum.OK.getValue());
            for (Long id : ids) {
                chatMsg.setId(id);
                chatMsgDao.updateById(chatMsg);
            }
        }
        this.supplyList(chatMsgList);
        result.setData(chatMsgList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 结果
     */
    @Override
    public ResultData<PageResult<ChatMsgResult>> pageChatMsgList(ChatMsgQuery query) {
        ResultData<PageResult<ChatMsgResult>> result = new ResultData<>();
        if (query == null) {
            query = new ChatMsgQuery();
        }
        PageResult<ChatMsgResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询我的消息列表
     * @param query 查询参数
     * @return 结果
     */
    @Override
    public ResultData<PageResult<ChatMsgResult>> pageMyChatMsgList(ChatMsgQuery query) {
        ResultData<PageResult<ChatMsgResult>> result = new ResultData<>();
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        if (query == null) {
            query = new ChatMsgQuery();
        }
        query.setOrSenderId(userId);
        query.setOrReceiverId(userId);
        PageResult<ChatMsgResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 保存聊天消息附件
     * @param msgId 聊天消息id
     * @param refs 参考文档列表
     * @param userId 发送人id
     */
    private void saveChatMsgAttList(Long msgId, List<ChatMsgAttResult> refs, Long userId) {
        if (msgId == null || CollectionUtil.isEmpty(refs)) {
            return;
        }
        try {
            // 根据docId去重后保存
            List<ChatMsgAtt> list = this.dedupByDocId(refs).stream().map(ref -> {
                ChatMsgAtt att = new ChatMsgAtt();
                att.setMsgId(msgId);
                att.setMsgAttType(ChatMsgAttTypeEnum.RAG_REFERENCE.getValue());
                att.setDocId(ref.getDocId());
                att.setDocName(ref.getDocName());
                att.setCreatedBy(userId);
                att.setUpdatedBy(userId);
                return att;
            }).collect(toList());
            chatMsgAttDao.batchInsert(list);
        } catch (Exception e) {
            logger.error("saveChatMsgAtts error, msgId={}", msgId, e);
        }
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<ChatMsgResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        List<Long> msgIds = list.stream().map(ChatMsgResult::getId).toList();
        ChatMsgAttQuery attQuery = new ChatMsgAttQuery();
        attQuery.setMsgIds(msgIds);
        attQuery.setMsgAttType(ChatMsgAttTypeEnum.RAG_REFERENCE.getValue());
        List<ChatMsgAttResult> attList = chatMsgAttDao.queryChatMsgAttList(attQuery);
        Map<Long, List<ChatMsgAttResult>> attMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(attList)) {
            // 按msgId分组后，每组按docId去重
            attMap = attList.stream()
                    .collect(Collectors.groupingBy(ChatMsgAttResult::getMsgId))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> this.dedupByDocId(e.getValue())));
        }
        Map<Long, List<ChatMsgAttResult>> finalAttMap = attMap;
        list.forEach(item -> {
            Long senderId = item.getSenderId();
            item.setSenderName(super.getObjName(senderId));
            Long receiverId = item.getReceiverId();
            item.setReceiverName(super.getObjName(receiverId));
            Integer signStatus = item.getSignStatus();
            if (ChatMsgStatusEnum.NO.getValue().equals(signStatus)) {
                item.setSignStatusName("未签收");
            } else {
                item.setSignStatusName("已签收");
            }
            Integer readStatus = item.getReadStatus();
            if (ChatMsgStatusEnum.NO.getValue().equals(readStatus)) {
                item.setReadStatusName("未读");
            } else {
                item.setReadStatusName("已读");
            }
            item.setReferences(finalAttMap.get(item.getId()));
        });
    }

    /**
     * 根据docId去重参考文件列表（保留首条，按列表原顺序）
     * @param list 参考文件列表
     * @return 去重后的列表
     */
    private List<ChatMsgAttResult> dedupByDocId(List<ChatMsgAttResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return list;
        }
        Set<Long> seen = new HashSet<>();
        List<ChatMsgAttResult> result = new ArrayList<>();
        for (ChatMsgAttResult item : list) {
            Long docId = item.getDocId();
            if (docId == null || seen.add(docId)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return  数量
     */
    @Override
    protected int queryCount(ChatMsgQuery query) {
        return chatMsgDao.queryChatMsgCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<ChatMsgResult> queryList(ChatMsgQuery query) {
        return chatMsgDao.queryChatMsgList(query);
    }
}
