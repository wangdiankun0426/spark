package com.spark.manage.auth.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.EncryptKey;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.utils.StringUtil;
import com.spark.config.redis.RedisService;
import com.spark.manage.auth.IEncryptKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-12 16:00:00
 * 一次性加密密钥服务
 * 禁止添加日志切面注解，避免下发的密钥被打印到日志
 */
@Service
public class EncryptKeyServiceImpl implements IEncryptKeyService {

    /**
     * 密钥字符集，全ASCII字符保证一个字符占一个字节
     */
    private static final String KEY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 密钥长度，DES密钥固定8字节
     */
    private static final int KEY_LENGTH = 8;

    /**
     * 密钥有效期，单位秒
     */
    private static final long KEY_EXPIRE_SECONDS = 2 * 60;

    /**
     * 随机数生成器
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private RedisService redisService;

    /**
     * 生成一次性加密密钥
     * @return 一次性密钥
     */
    @Override
    public ResultData<EncryptKey> generateEncryptKey() {
        ResultData<EncryptKey> result = new ResultData<>();
        String keyId = UUID.randomUUID().toString();
        String key = this.randomKey();
        String redisKey = ObjectCacheKey.ENCRYPT_KEY + keyId;
        boolean bo = redisService.setStr(redisKey, key, KEY_EXPIRE_SECONDS);
        if (!bo) {
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
            return result;
        }
        EncryptKey encryptKey = new EncryptKey();
        encryptKey.setKeyId(keyId);
        encryptKey.setKey(key);
        result.setData(encryptKey);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 按密钥id取出密钥并删除
     * @param keyId 密钥id
     * @return 一次性密钥，不存在返回null
     */
    @Override
    public String consumeEncryptKey(String keyId) {
        if (StringUtil.isBlank(keyId)) {
            return null;
        }
        String redisKey = ObjectCacheKey.ENCRYPT_KEY + keyId;
        return redisService.getAndDel(redisKey);
    }

    /**
     * 生成随机密钥
     * @return 8位随机密钥
     */
    private String randomKey() {
        StringBuilder builder = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = SECURE_RANDOM.nextInt(KEY_CHARS.length());
            char ch = KEY_CHARS.charAt(index);
            builder.append(ch);
        }
        return builder.toString();
    }
}
