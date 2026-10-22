package com.spark.web.ws;

import com.spark.common.utils.SpringUtil;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.entity.ChatMsg;
import com.spark.common.bean.chat.vo.ChatMsgVO;
import com.spark.common.bean.chat.vo.DataContentVO;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.chat.service.IChatMsgService;
import com.spark.chat.service.UserChannelRel;
import com.spark.common.enums.MsgActionEnum;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.redis.RedisService;
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
 * WebSocket端点，握手经WsAuthConfigurator校验登录会话，连接身份以服务端会话为准
 */
@Component
@ServerEndpoint(value = "/ws/chat", configurator = WsAuthConfigurator.class)
public class ChatWebSocketEndpoint {
    private final static Logger logger = LoggerFactory.getLogger(ChatWebSocketEndpoint.class);

    /**
     * 连接上绑定的用户id属性名
     */
    private final static String USER_ID_PROP = "userId";

    /**
     * 握手鉴权后业务会话在连接属性中的键名
     */
    public final static String LOGIN_SESSION_PROP = "loginSession";

    /**
     * 未授权关闭码
     */
    private final static int CLOSE_UNAUTHORIZED = 4401;

    /**
     * 会话吊销重查间隔
     */
    private final static long REVALIDATE_INTERVAL_MS = 5 * 60 * 1000L;

    /**
     * 登录会话滑动过期时间
     */
    private final static int SESSION_EXPIRE_SECONDS = 60 * 60;

    /**
     * 当前连接绑定的登录会话
     */
    private com.spark.common.bean.sys.entity.Session loginSession;

    /**
     * 上次会话校验时间
     */
    private long lastValidateTime;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        Object prop = session.getUserProperties().get(LOGIN_SESSION_PROP);
        if (!(prop instanceof com.spark.common.bean.sys.entity.Session)) {
            logger.warn("ws connection rejected, no valid login session");
            this.closeUnauthorized(session);
            return;
        }
        this.loginSession = (com.spark.common.bean.sys.entity.Session) prop;
        this.lastValidateTime = System.currentTimeMillis();
        logger.info("ws connection opened, userId={}, sessionId={}", this.loginSession.getUserId(), session.getId());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        logger.info("ws connection closed, sessionId={}", session.getId());
        UserChannelRel.removeBySession(session);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        if (this.loginSession == null || this.loginSession.getUserId() == null) {
            this.closeUnauthorized(session);
            return;
        }
        if (!this.revalidate(session)) {
            return;
        }
        logger.info("ws get message, message={}", message);
        // 获取客户端发来的消息
        DataContentVO dataContent = JsonUtil.toObject(message, DataContentVO.class);
        if (dataContent == null || dataContent.getAction() == null) {
            logger.error("ws params incomplete");
            return;
        }
        Integer action = dataContent.getAction();
        Long authUserId = this.loginSession.getUserId();
        // 判断消息类型
        if (MsgActionEnum.CONNECT.getValue().equals(action)) {
            if (dataContent.getChatMsg() != null && dataContent.getChatMsg().getSenderId() != null
                    && !authUserId.equals(dataContent.getChatMsg().getSenderId())) {
                logger.warn("ws connect senderId mismatch, expect={}, actual={}", authUserId, dataContent.getChatMsg().getSenderId());
            }
            this.connectChannel(authUserId, session);
        } else if (MsgActionEnum.CHAT_MSG.getValue().equals(action)) {
            handleChatMsg(dataContent, session, authUserId);
        } else if (MsgActionEnum.SIGN_MSG.getValue().equals(action)) {
            handleSignMsg(dataContent, authUserId);
        } else if (MsgActionEnum.READ_MSG.getValue().equals(action)) {
            handleReadMsg(dataContent, authUserId);
        } else if (MsgActionEnum.PONG.getValue().equals(action)) {
            // 心跳类型的消息
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("ws error, sessionId={}", session.getId(), error);
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
    private void handleChatMsg(DataContentVO dataContent, Session session, Long authUserId) {
        ChatMsg chatMsg = dataContent.getChatMsg();
        if (chatMsg == null || StringUtil.isBlank(chatMsg.getMessage())
                || chatMsg.getReceiverId() == null || chatMsg.getSpaceId() == null) {
            logger.error("ws create fail, params incomplete");
            return;
        }
        Long senderId = authUserId;
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
                logger.info("ws receiver not online, send msg fail");
                return;
            }
            UserChannelRel.sendMessage(receiverSessions, dataContent, null);
            logger.info("ws send msg success, size={}", receiverSessions.size());
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
     * @param dataContent 消息内容
     * @param authUserId 当前登录用户id
     */
    private void handleSignMsg(DataContentVO dataContent, Long authUserId) {
        ChatMsg chatMsg = dataContent.getChatMsg();
        if (chatMsg == null || chatMsg.getId() == null) {
            logger.error("ws msg sign fail, params incomplete");
            return;
        }
        ChatMsgVO chatMsgVO = new ChatMsgVO();
        chatMsgVO.setId(chatMsg.getId());
        chatMsgVO.setReceiverId(authUserId);
        IChatMsgService chatMsgService = SpringUtil.getClass(IChatMsgService.class);
        ResultData<Void> signChatMsgData = chatMsgService.signChatMsg(chatMsgVO);
        if (signChatMsgData.getCode() != ResultData.OK) {
            logger.error("ws msg sign fail");
        }
    }

    /**
     * 处理已读消息
     * @param dataContent 消息内容
     * @param authUserId 当前登录用户id
     */
    private void handleReadMsg(DataContentVO dataContent, Long authUserId) {
        ChatMsg chatMsg = dataContent.getChatMsg();
        if (chatMsg == null || chatMsg.getId() == null) {
            logger.error("ws msg read fail, params incomplete");
            return;
        }
        ChatMsgVO chatMsgVO = new ChatMsgVO();
        chatMsgVO.setId(chatMsg.getId());
        chatMsgVO.setReceiverId(authUserId);
        IChatMsgService chatMsgService = SpringUtil.getClass(IChatMsgService.class);
        ResultData<Void> signChatMsgData = chatMsgService.readChatMsg(chatMsgVO);
        if (signChatMsgData.getCode() != ResultData.OK) {
            logger.error("ws msg read fail");
        }
    }

    /**
     * 以未授权码关闭连接
     * @param session websocket会话
     */
    private void closeUnauthorized(Session session) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(CLOSE_UNAUTHORIZED), "unauthorized"));
        } catch (IOException e) {
            logger.error("ws close unauthorized fail, sessionId={}", session.getId(), e);
        }
    }

    /**
     * 周期重查登录会话，登出或被踢后关闭连接
     * @param session websocket会话
     * @return 会话仍有效返回true
     */
    private boolean revalidate(Session session) {
        long now = System.currentTimeMillis();
        if (now - this.lastValidateTime < REVALIDATE_INTERVAL_MS) {
            return true;
        }
        RedisService redisService = SpringUtil.getClass(RedisService.class);
        String sessionKey = ObjectCacheKey.LOGIN_SESSION + this.loginSession.getSessionId();
        String sessionJson = redisService.getValue(sessionKey);
        if (StringUtil.isBlank(sessionJson)) {
            logger.warn("ws session revoked, userId={}", this.loginSession.getUserId());
            UserChannelRel.removeBySession(session);
            this.closeUnauthorized(session);
            return false;
        }
        redisService.expire(sessionKey, SESSION_EXPIRE_SECONDS);
        this.lastValidateTime = now;
        return true;
    }
}
