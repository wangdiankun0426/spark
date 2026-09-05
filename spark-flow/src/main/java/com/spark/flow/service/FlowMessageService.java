package com.spark.flow.service;

import com.spark.common.bean.base.ResultData;

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
