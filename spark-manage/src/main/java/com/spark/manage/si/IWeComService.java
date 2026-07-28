package com.spark.manage.si;

import com.spark.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 10:02:00
 * 企业微信服务接口
 */
public interface IWeComService {

    /**
     * 处理企业微信消息
     * @param msg 解密后的 XML 消息内容
     * @return 回复消息内容（为空字符串表示不回复）
     */
    ResultData<String> handleWeComMsg(String msg);

    /**
     * 全量同步企业微信组织架构
     * 同步所有部门及用户到本地系统
     * @return 同步结果
     */
    ResultData<String> syncWeComOrganization();

}