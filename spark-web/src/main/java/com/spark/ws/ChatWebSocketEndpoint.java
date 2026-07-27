package com.spark.ws;

import com.spark.utils.SpringUtil;
import com.spark.bean.base.ResultData;
import com.spark.bean.chat.entity.ChatMsg;
import com.spark.bean.chat.vo.ChatMsgVO;
import com.spark.bean.chat.vo.DataContentVO;
import com.spark.enums.ObjectTypeEnum;
import com.spark.chat.service.IChatMsgService;
import com.spark.chat.service.UserChannelRel;
import com.spark.enums.MsgActionEnum;
import com.spark.utils.JsonUtil;
import com.spark.utils.StringUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/14 17:50
 * WebSocket端点,使用注解方式实现
 * 使用SpringConfigurator支持@Autowired依赖注入
 */
@Component
@ServerEndpoint(value = "/ws/chat")
public class ChatWebSocketEndpoint {
    private final static Logger logger = LoggerFactory.getLogger(ChatWebSocketEndpoint.class);

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        logger.info("ws connection opened={}", session.getId());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        logger.info("ws connection closed={}", session.getId());
        UserChannelRel.removeBySession(session);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("ws get message{}", message);
        // 获取客户端发来的消息
        DataContentVO dataContent = JsonUtil.toObject(message, DataContentVO.class);
        if (dataContent == null || dataContent.getAction() == null) {
            logger.error("ws params no full");
            return;
        }
        Integer action = dataContent.getAction();
        // 判断消息类型
        if (MsgActionEnum.CONNECT.getValue().equals(action)) {
            // 当webSocket第一次open/重连的时候 初始化session把用的session和userId进行关联
            if (dataContent.getChatMsg() == null || dataContent.getChatMsg().getSenderId() == null) {
                logger.error("ws connect params no full");
                return;
            }
            Long senderId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(senderId, session);
        } else if (MsgActionEnum.CHAT_MSG.getValue().equals(action)) {
            handleChatMsg(dataContent, session);
        } else if (MsgActionEnum.SIGN_MSG.getValue().equals(action)) {
            handleSignMsg(dataContent);
        } else if (MsgActionEnum.READ_MSG.getValue().equals(action)) {
            handleReadMsg(dataContent);
        } else if (MsgActionEnum.PONG.getValue().equals(action)) {
            // 心跳类型的消息
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("ws error: {}", session.getId(), error);
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            logger.error("close session error", e);
        }
        UserChannelRel.removeBySession(session);
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMsg(DataContentVO dataContent, Session session) {
        ChatMsg chatMsg = dataContent.getChatMsg();
        if (chatMsg == null || StringUtil.isBlank(chatMsg.getMessage()) || chatMsg.getSenderId() == null
                || chatMsg.getReceiverId() == null || chatMsg.getSpaceId() == null) {
            logger.error("ws create fail, params no full");
            return;
        }
        Long senderId = chatMsg.getSenderId();
        Long receiverId = chatMsg.getReceiverId();
        ChatMsgVO chatMsgVO = new ChatMsgVO();
        chatMsgVO.setMessage(chatMsg.getMessage());
        chatMsgVO.setSenderId(senderId);
        chatMsgVO.setReceiverId(receiverId);
        chatMsgVO.setSpaceId(chatMsg.getSpaceId());
        chatMsgVO.setModelId(chatMsg.getModelId());
        IChatMsgService chatMsgService = SpringUtil.getClass(IChatMsgService.class);
        ResultData<ChatMsg> createData = chatMsgService.createChatMsg(chatMsgVO);
        if (createData.getCode() != ResultData.OK) {
            logger.info("ws create fail");
            return;
        }
        dataContent.setChatMsg(createData.getData());
        // 发送人和接收人一致 则不需要发送
        if (receiverId.equals(senderId)) {
            return;
        }
        ObjectTypeEnum objEnum = this.getObjEnum(receiverId);
        if (objEnum == ObjectTypeEnum.USER) {
            // 普通用户消息
            Session receiverSession = UserChannelRel.get(receiverId);
            if (receiverSession == null || !receiverSession.isOpen()) {
                // 离线 接收用户的ws未连接
                logger.info("ws receiver no online, send msg fail");
                return;
            }
            // 在线 接收用户的ws已连接
            try {
                receiverSession.getBasicRemote().sendText(JsonUtil.toString(dataContent));
                logger.info("ws send msg success");
            } catch (IOException e) {
                logger.error("send message error", e);
            }
        } else if (objEnum == ObjectTypeEnum.AGENT) {
            Session senderSession = UserChannelRel.get(senderId);
            if (senderSession == null || !senderSession.isOpen()) {
                logger.error("agent ws sender session is invalid");
                return;
            }
            chatMsgService.createAgentChatMsg(chatMsgVO, senderSession);
        } else if (objEnum == ObjectTypeEnum.MODEL) {
            Session senderSession = UserChannelRel.get(senderId);
            if (senderSession == null || !senderSession.isOpen()) {
                logger.error("model ws sender session is invalid");
                return;
            }
            chatMsgService.createModelChatMsg(chatMsgVO, senderSession);
        }
    }

    /**
     * 获取对象枚举
     * @param objId 对象id
     * @return 对象枚举
     */
    public ObjectTypeEnum getObjEnum(Long objId) {
        //获取对象类型
        ObjectTypeEnum objType = ObjectTypeEnum.UNKNOWN;
        if(objId != null) {
            Integer intObjType = Integer.valueOf(objId % 100+"");
            objType = ObjectTypeEnum.indexOf(intObjType);
        }
        return objType;
    }

    /**
     * 处理签收消息
     */
    private void handleSignMsg(DataContentVO dataContent) {
        ChatMsg chatMsg = dataContent.getChatMsg();
        if (chatMsg == null || chatMsg.getId() == null) {
            logger.error("ws msg sign fail, param no full");
            return;
        }
        ChatMsgVO chatMsgVO = new ChatMsgVO();
        chatMsgVO.setId(chatMsg.getId());
        IChatMsgService chatMsgService = SpringUtil.getClass(IChatMsgService.class);
        ResultData<Void> signChatMsgData = chatMsgService.signChatMsg(chatMsgVO);
        if (signChatMsgData.getCode() != ResultData.OK) {
            logger.error("ws msg sign fail");
        }
    }

    /**
     * 处理已读消息
     */
    private void handleReadMsg(DataContentVO dataContent) {
        ChatMsg chatMsg = dataContent.getChatMsg();
        if (chatMsg == null || chatMsg.getId() == null) {
            logger.error("ws msg read fail, param no full");
            return;
        }
        ChatMsgVO chatMsgVO = new ChatMsgVO();
        chatMsgVO.setId(chatMsg.getId());
        IChatMsgService chatMsgService = SpringUtil.getClass(IChatMsgService.class);
        ResultData<Void> signChatMsgData = chatMsgService.readChatMsg(chatMsgVO);
        if (signChatMsgData.getCode() != ResultData.OK) {
            logger.error("ws msg read fail");
        }
    }
}
