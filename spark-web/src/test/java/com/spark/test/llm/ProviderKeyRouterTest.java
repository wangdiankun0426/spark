package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ProviderKeyCredential;
import com.spark.common.bean.llm.entity.ProviderKey;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.dao.llm.ProviderKeyDao;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.credential.ProviderKeyRoute;
import com.spark.llm.model.credential.ProviderKeyRouter;
import com.spark.llm.security.LlmApiKeyCipher;
import dev.langchain4j.exception.HttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:20:00
 * 供应商凭据轮询与冷却测试
 */
public class ProviderKeyRouterTest {
    private ProviderKeyDao providerKeyDao;
    private LlmApiKeyCipher llmApiKeyCipher;
    private ProviderKeyRouter providerKeyRouter;

    @BeforeEach
    void setUp() {
        providerKeyDao = mock(ProviderKeyDao.class);
        llmApiKeyCipher = mock(LlmApiKeyCipher.class);
        providerKeyRouter = new ProviderKeyRouter();
        ReflectionTestUtils.setField(providerKeyRouter, "providerKeyDao", providerKeyDao);
        ReflectionTestUtils.setField(providerKeyRouter, "llmApiKeyCipher", llmApiKeyCipher);
    }

    /**
     * 验证两个可用Key连续选择时按1、2、1轮询，并且只解密当前选中的密文
     */
    @Test
    void shouldRoundRobinAvailableKeys() {
        ProviderKeyCredential first = this.createCredential(1L, "cipher-1");
        ProviderKeyCredential second = this.createCredential(2L, "cipher-2");
        when(providerKeyDao.queryAvailableProviderKeyList(10L)).thenReturn(List.of(first, second));
        when(llmApiKeyCipher.decrypt("cipher-1")).thenReturn("plain-1");
        when(llmApiKeyCipher.decrypt("cipher-2")).thenReturn("plain-2");
        ProviderKeyRoute firstRoute = providerKeyRouter.select(10L);
        ProviderKeyRoute secondRoute = providerKeyRouter.select(10L);
        ProviderKeyRoute thirdRoute = providerKeyRouter.select(10L);
        assertEquals(1L, firstRoute.getProviderKeyId());
        assertEquals("plain-1", firstRoute.getSecretKey());
        assertEquals(2L, secondRoute.getProviderKeyId());
        assertEquals("plain-2", secondRoute.getSecretKey());
        assertEquals(1L, thirdRoute.getProviderKeyId());
        verify(llmApiKeyCipher).decrypt("cipher-2");
    }

    /**
     * 验证凭据池没有可用Key时立即返回RATE_LIMIT，不解密也不等待冷却结束
     */
    @Test
    void shouldFailFastWhenNoAvailableKey() {
        when(providerKeyDao.queryAvailableProviderKeyList(10L)).thenReturn(List.of());
        ModelInvocationException error = assertThrows(ModelInvocationException.class,
                () -> providerKeyRouter.select(10L));
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, error.getErrorType());
        assertEquals(429, error.getHttpStatus());
        verify(llmApiKeyCipher, never()).decrypt(any());
    }

    /**
     * 验证429只将当前Key冷却到数据库时间后60秒，后续403认证错误不会再次写冷却
     */
    @Test
    void shouldCooldownOnlyRateLimitedKey() {
        Timestamp currentTime = new Timestamp(1000000L);
        when(providerKeyDao.queryCurrentTime()).thenReturn(currentTime);
        when(providerKeyDao.updateDBById(any())).thenReturn(1);
        HttpException rateLimitError = new HttpException(429, "limited");
        providerKeyRouter.handleError(10L, 11L, rateLimitError);
        ArgumentCaptor<ProviderKey> keyCaptor = ArgumentCaptor.forClass(ProviderKey.class);
        verify(providerKeyDao).updateDBById(keyCaptor.capture());
        ProviderKey providerKey = keyCaptor.getValue();
        assertEquals(11L, providerKey.getId());
        assertEquals(currentTime.getTime() + 60000L, providerKey.getCooldownUntil().getTime());
        providerKeyRouter.handleError(10L, 12L, new HttpException(403, "forbidden"));
        verify(providerKeyDao).updateDBById(any());
    }

    /**
     * 验证429冷却写库失败时当前节点仍过滤该Key，下一次尝试直接选择另一把Key
     */
    @Test
    void shouldUseLocalCooldownWhenPersistenceFails() {
        ProviderKeyCredential first = this.createCredential(1L, "cipher-1");
        ProviderKeyCredential second = this.createCredential(2L, "cipher-2");
        when(providerKeyDao.queryAvailableProviderKeyList(10L)).thenReturn(List.of(first, second));
        when(providerKeyDao.queryCurrentTime()).thenThrow(new IllegalStateException("database unavailable"));
        when(llmApiKeyCipher.decrypt("cipher-2")).thenReturn("plain-2");
        providerKeyRouter.handleError(10L, 1L, new HttpException(429, "limited"));
        ProviderKeyRoute route = providerKeyRouter.select(10L);
        assertEquals(2L, route.getProviderKeyId());
        assertEquals("plain-2", route.getSecretKey());
        verify(llmApiKeyCipher, never()).decrypt("cipher-1");
    }

    /**
     * 验证清理厂商缓存游标后重新从第一把Key开始轮询，避免厂商变更继承旧位置
     */
    @Test
    void shouldResetSequenceWhenProviderCacheCleared() {
        ProviderKeyCredential first = this.createCredential(1L, "cipher-1");
        ProviderKeyCredential second = this.createCredential(2L, "cipher-2");
        when(providerKeyDao.queryAvailableProviderKeyList(10L)).thenReturn(List.of(first, second));
        when(llmApiKeyCipher.decrypt(any())).thenReturn("plain");
        providerKeyRouter.select(10L);
        providerKeyRouter.clearProvider(10L);
        ProviderKeyRoute route = providerKeyRouter.select(10L);
        assertEquals(1L, route.getProviderKeyId());
    }

    /**
     * 构造内部凭据查询结果
     */
    private ProviderKeyCredential createCredential(Long id, String encryptedKey) {
        ProviderKeyCredential credential = new ProviderKeyCredential();
        credential.setId(id);
        credential.setProviderId(10L);
        credential.setSecretKey(encryptedKey);
        return credential;
    }
}
