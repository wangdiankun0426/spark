package com.spark.flow.service;

import com.spark.bean.base.ResultData;
import com.spark.enums.MessageTypeEnum;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/8/6 21:34
 */
public interface FlowMessageService {

    /**
     * 发送流程通知
     * @param instanceId
     * @param msgType
     * @return
     */
    ResultData<Void> sendFlowNotice(String instanceId, Integer msgType);
}
