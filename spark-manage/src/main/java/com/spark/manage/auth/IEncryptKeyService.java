package com.spark.manage.auth;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.EncryptKey;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-12 16:00:00
 * 一次性加密密钥服务
 */
public interface IEncryptKeyService {

    /**
     * 生成一次性加密密钥
     * @return 一次性密钥
     */
    ResultData<EncryptKey> generateEncryptKey();

    /**
     * 按密钥id取出密钥并删除
     * @param keyId 密钥id
     * @return 一次性密钥，不存在返回null
     */
    String consumeEncryptKey(String keyId);
}
