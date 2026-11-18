package com.spark.llm.security;

import com.spark.common.utils.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥加解密组件
 */
@Component
public class LlmApiKeyCipher {
    private static final String VERSION_PREFIX = "v1:";
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int MASTER_KEY_LENGTH = 32;
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_TAG_LENGTH_BYTES = 16;
    private static final int LAST_KEY_LENGTH = 4;
    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public LlmApiKeyCipher(@Value("${spark.llm.api-key-encrypt-key}") String encodedKey) {
        if (StringUtil.isBlank(encodedKey)) {
            throw new IllegalArgumentException("模型密钥加密配置不能为空");
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            if (keyBytes.length != MASTER_KEY_LENGTH) {
                throw new IllegalArgumentException("模型密钥加密配置长度无效");
            }
            this.secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("模型密钥加密配置无效");
        }
    }

    /**
     * 加密厂商密钥
     * @param plainKey 明文密钥
     * @return 带版本号的密文
     */
    public String encrypt(String plainKey) {
        if (StringUtil.isBlank(plainKey)) {
            throw new IllegalArgumentException("厂商密钥不能为空");
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] plainBytes = plainKey.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(plainBytes);
            byte[] payload = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, payload, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, payload, IV_LENGTH, encryptedBytes.length);
            String encodedPayload = Base64.getEncoder().encodeToString(payload);
            return VERSION_PREFIX + encodedPayload;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("厂商密钥加密失败");
        }
    }

    /**
     * 解密厂商密钥
     * @param encryptedKey 带版本号的密文
     * @return 明文密钥
     */
    public String decrypt(String encryptedKey) {
        if (StringUtil.isBlank(encryptedKey)) {
            throw new IllegalArgumentException("厂商密钥密文无效");
        }
        if (!encryptedKey.startsWith(VERSION_PREFIX)) {
            throw new IllegalArgumentException("厂商密钥密文无效");
        }
        try {
            int prefixLength = VERSION_PREFIX.length();
            String encodedPayload = encryptedKey.substring(prefixLength);
            byte[] payload = Base64.getDecoder().decode(encodedPayload);
            if (payload.length < IV_LENGTH + GCM_TAG_LENGTH_BYTES) {
                throw new IllegalArgumentException("厂商密钥密文无效");
            }
            byte[] iv = new byte[IV_LENGTH];
            int encryptedLength = payload.length - IV_LENGTH;
            byte[] encryptedBytes = new byte[encryptedLength];
            System.arraycopy(payload, 0, iv, 0, IV_LENGTH);
            System.arraycopy(payload, IV_LENGTH, encryptedBytes, 0, encryptedLength);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] plainBytes = cipher.doFinal(encryptedBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalArgumentException("厂商密钥密文无效");
        }
    }

    /**
     * 判断是否为受支持的密文格式
     * @param value 待判断内容
     * @return 是否为v1密文
     */
    public boolean isEncrypted(String value) {
        if (StringUtil.isBlank(value)) {
            return false;
        }
        if (!value.startsWith(VERSION_PREFIX)) {
            return false;
        }
        try {
            int prefixLength = VERSION_PREFIX.length();
            String encodedPayload = value.substring(prefixLength);
            byte[] payload = Base64.getDecoder().decode(encodedPayload);
            return payload.length >= IV_LENGTH + GCM_TAG_LENGTH_BYTES;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 获取密钥末四位
     * @param plainKey 明文密钥
     * @return 末四位
     */
    public String last4(String plainKey) {
        if (StringUtil.isBlank(plainKey)) {
            throw new IllegalArgumentException("厂商密钥不能为空");
        }
        int keyLength = plainKey.length();
        int startIndex = Math.max(0, keyLength - LAST_KEY_LENGTH);
        return plainKey.substring(startIndex);
    }

}
