package com.spark.flow.listener;

import jakarta.annotation.PostConstruct;
import org.flowable.common.engine.api.delegate.event.FlowableEventDispatcher;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/1 14:44
 */
@Component
public class FlowableEventListenerRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(FlowableEventListenerRegistrar.class);

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private GlobalFlowableEventListener globalFlowableEventListener;

    @PostConstruct
    public void registerEventListener() {
        try {
            // 获取配置
            ProcessEngineConfigurationImpl config =
                    (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
            // 获取事件调度器
            FlowableEventDispatcher eventDispatcher = config.getEventDispatcher();
            // 添加监听器
            eventDispatcher.addEventListener(globalFlowableEventListener);
            logger.info("Flowable EventListener by EventDispatcher!");
        } catch (Exception e) {
            logger.error("Flowable EventListener error", e);
        }
    }
}