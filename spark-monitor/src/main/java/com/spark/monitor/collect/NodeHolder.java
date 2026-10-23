package com.spark.monitor.collect;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 节点标识持有者
 */
@Component
public class NodeHolder {

    private final static Logger logger = LoggerFactory.getLogger(NodeHolder.class);

    @Value("${server.port:8000}")
    private int serverPort;

    /**
     * 节点标识
     */
    private String nodeKey;

    /**
     * 启动时解析主机名拼接节点标识
     */
    @PostConstruct
    public void init() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostName = "unknown";
            logger.warn("resolve local hostname error, use default, errMsg={}", e.getMessage());
        }
        nodeKey = hostName + ":" + serverPort;
        logger.info("monitor nodeKey init, nodeKey={}", nodeKey);
    }

    /**
     * 获取当前节点标识
     * @return 节点标识
     */
    public String getNodeKey() {
        return nodeKey;
    }
}
