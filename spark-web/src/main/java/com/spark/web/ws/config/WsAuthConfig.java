package com.spark.web.ws.config;

import com.spark.common.bean.sys.entity.Session;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.SpringUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.redis.RedisService;
import com.spark.web.ws.ChatWebSocketEndpoint;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-22 10:00:00
 * WebSocket握手鉴权器，校验登录会话并把身份透传给连接
 */
public class WsAuthConfig extends ServerEndpointConfig.Configurator {
    private static final Logger logger = LoggerFactory.getLogger(WsAuthConfig.class);

    /**
     * 握手时校验登录会话，通过则把业务会话放入连接属性
     * 校验失败不放身份，由端点onOpen关闭连接
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        String sessionId = this.resolveSessionId(request);
        if (StringUtil.isBlank(sessionId)) {
            return;
        }
        try {
            RedisService redisService = SpringUtil.getClass(RedisService.class);
            String sessionJson = redisService.getValue(ObjectCacheKey.LOGIN_SESSION + sessionId);
            if (StringUtil.isBlank(sessionJson)) {
                return;
            }
            Session loginSession = JsonUtil.toObject(sessionJson, Session.class);
            if (loginSession == null || loginSession.getUserId() == null) {
                return;
            }
            config.getUserProperties().put(ChatWebSocketEndpoint.LOGIN_SESSION_PROP, loginSession);
        } catch (Exception e) {
            // 坏会话不阻断握手流程，交由端点按未授权关闭
            logger.warn("ws handshake resolve session fail", e);
        }
    }

    /**
     * 解析登录会话id，优先取query参数再取Authorization头
     * @param request 握手请求
     * @return 会话id
     */
    private String resolveSessionId(HandshakeRequest request) {
        Map<String, List<String>> params = request.getParameterMap();
        if (params != null) {
            List<String> values = params.get("sessionId");
            if (values != null && !values.isEmpty() && StringUtil.isNotBlank(values.get(0))) {
                return values.get(0);
            }
        }
        Map<String, List<String>> headers = request.getHeaders();
        if (headers != null) {
            List<String> authValues = headers.get("Authorization");
            if (authValues != null && !authValues.isEmpty()) {
                return authValues.get(0);
            }
        }
        return null;
    }
}
