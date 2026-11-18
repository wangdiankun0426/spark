package com.spark.test.llm;

import com.spark.llm.security.LlmApiKeyCipher;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:30:00
 * 模型厂商密钥加解密测试
 */
public class LlmApiKeyCipherTest {
    private static final String TEST_KEY = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

    /**
     * 验证同一明文两次加密使用随机IV得到不同v1密文，两次密文均可还原且格式识别正确
     */
    @Test
    void shouldEncryptWithRandomIvAndDecrypt() {
        LlmApiKeyCipher cipher = new LlmApiKeyCipher(TEST_KEY);
        String plainKey = "sk-test-123456";
        String firstEncryptedKey = cipher.encrypt(plainKey);
        String secondEncryptedKey = cipher.encrypt(plainKey);
        assertNotEquals(firstEncryptedKey, secondEncryptedKey);
        assertTrue(firstEncryptedKey.startsWith("v1:"));
        assertTrue(cipher.isEncrypted(firstEncryptedKey));
        assertEquals(plainKey, cipher.decrypt(firstEncryptedKey));
        assertEquals(plainKey, cipher.decrypt(secondEncryptedKey));
    }

    /**
     * 验证明文、伪造v1内容与被篡改的密文均无法解密，不会回退为明文使用
     */
    @Test
    void shouldRejectPlaintextAndTamperedCiphertext() {
        LlmApiKeyCipher cipher = new LlmApiKeyCipher(TEST_KEY);
        String plainKey = "sk-test-abcdef";
        String encryptedKey = cipher.encrypt(plainKey);
        assertThrows(IllegalArgumentException.class, () -> cipher.decrypt(plainKey));
        assertFalse(cipher.isEncrypted(plainKey));
        assertFalse(cipher.isEncrypted("v1:not-base64"));
        int prefixLength = "v1:".length();
        String encodedPayload = encryptedKey.substring(prefixLength);
        byte[] payload = Base64.getDecoder().decode(encodedPayload);
        payload[12] = (byte) (payload[12] ^ 1);
        String tamperedPayload = Base64.getEncoder().encodeToString(payload);
        String tamperedKey = "v1:" + tamperedPayload;
        assertThrows(IllegalArgumentException.class, () -> cipher.decrypt(tamperedKey));
    }

    /**
     * 验证主密钥为空、Base64非法或解码后不是32字节时构造失败，异常信息不包含原始配置
     */
    @Test
    void shouldRejectInvalidMasterKey() {
        assertThrows(IllegalArgumentException.class, () -> new LlmApiKeyCipher(" "));
        assertThrows(IllegalArgumentException.class, () -> new LlmApiKeyCipher("not-base64"));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new LlmApiKeyCipher("AAECAwQFBgcICQoLDA0ODw=="));
        assertFalse(error.getMessage().contains("AAECAwQFBgcICQoLDA0ODw=="));
    }

    /**
     * 验证长密钥只保留末四位、短密钥保留全部内容、空白密钥直接拒绝
     */
    @Test
    void shouldResolveLast4() {
        LlmApiKeyCipher cipher = new LlmApiKeyCipher(TEST_KEY);
        assertEquals("cdef", cipher.last4("abcdef"));
        assertEquals("abc", cipher.last4("abc"));
        assertThrows(IllegalArgumentException.class, () -> cipher.last4(" "));
    }

}
