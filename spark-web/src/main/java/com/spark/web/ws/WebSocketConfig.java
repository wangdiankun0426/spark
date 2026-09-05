package com.spark.web.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/14 17:36
 * WebSocket配置类，用于支持@ServerEndpoint注解
 */
@Configuration
public class WebSocketConfig {

    /**
     * 这个Bean会自动注册使用@ServerEndpoint注解声明的WebSocket endpoint
     * 并且支持Spring的依赖注入
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
