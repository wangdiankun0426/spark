package com.spark.web.ws;

import com.spark.common.utils.SpringUtil;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.entity.ChatMsg;
import com.spark.common.bean.chat.vo.ChatMsgVO;
import com.spark.common.bean.chat.vo.DataContentVO;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.chat.service.IChatMsgService;
import com.spark.chat.service.UserChannelRel;
import com.spark.common.enums.MsgActionEnum;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

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
     * 连接上绑定的用户id属性名
     */
    private final static String USER_ID_PROP = "userId";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        //todo 鉴权
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
            this.connectChannel(senderId, session);
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
     * 绑定连接到用户，同一账号多终端登录时保留该用户的全部连接
     * @param senderId 用户id
     * @param session websocket会话
     */
    private void connectChannel(Long senderId, Session session) {
        // 同一连接重复注册时先解绑旧用户，避免异常客户端先后注册多个账号后收到多份广播
        Object bindUserId = session.getUserProperties().get(USER_ID_PROP);
        if (bindUserId != null && !bindUserId.equals(senderId)) {
            UserChannelRel.removeBySession(session);
        }
        session.getUserProperties().put(USER_ID_PROP, senderId);
        UserChannelRel.put(senderId, session);
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
        ChatMsg createChatMsg = createData.getData();
        chatMsgVO.setId(createChatMsg.getId());
        dataContent.setChatMsg(createChatMsg);
        if (receiverId.equals(senderId)) {
            // 发送人和接收人一致 则不需要发送
            return;
        }
        // 回推给发送者的其他终端，排除当前连接（当前端已本地插入，回推会重复）
        // 必须放在分支之前，保证AI场景下其他终端先看到提问再看到流式回答
        UserChannelRel.sendMessage(UserChannelRel.get(senderId), dataContent, session);
        ObjectTypeEnum objEnum = this.getObjEnum(receiverId);
        if (objEnum == ObjectTypeEnum.USER) {
            // 普通用户消息，推送给接收者的全部在线连接
            List<Session> receiverSessions = UserChannelRel.get(receiverId);
            if (receiverSessions.isEmpty()) {
                // 离线 接收用户的ws未连接
                logger.info("ws receiver no online, send msg fail");
                return;
            }
            UserChannelRel.sendMessage(receiverSessions, dataContent, null);
            logger.info("ws send msg success, receiver session size={}", receiverSessions.size());
        } else if (objEnum == ObjectTypeEnum.AGENT) {
            // AI回复推送给提问账号的全部在线连接，包含发起提问的当前连接
            List<Session> senderSessions = UserChannelRel.get(senderId);
            if (senderSessions.isEmpty()) {
                logger.error("agent ws sender session is invalid");
                return;
            }
            chatMsgService.createAgentChatMsg(chatMsgVO, senderSessions);
        } else if (objEnum == ObjectTypeEnum.MODEL) {
            // AI回复推送给提问账号的全部在线连接，包含发起提问的当前连接
            List<Session> senderSessions = UserChannelRel.get(senderId);
            if (senderSessions.isEmpty()) {
                logger.error("model ws sender session is invalid");
                return;
            }
            chatMsgService.createModelChatMsg(chatMsgVO, senderSessions);
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
