package com.spark.llm.model.credential;

import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.error.ModelInvocationException;

import com.spark.common.bean.llm.dto.ProviderKeyCredential;
import com.spark.common.bean.llm.entity.ProviderKey;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.dao.llm.ProviderKeyDao;
import com.spark.llm.security.LlmApiKeyCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:00:00
 * 供应商凭据轮询与限流冷却路由器
 */
@Component
public class ProviderKeyRouter {
    private static final Logger logger = LoggerFactory.getLogger(ProviderKeyRouter.class);
    /**
     * 凭据触发限流后的冷却毫秒数
     */
    private static final long COOLDOWN_MS = 60000L;
    /**
     * 凭据与冷却时间数据访问对象
     */
    @Autowired
    private ProviderKeyDao providerKeyDao;
    /**
     * 凭据加解密组件
     */
    @Autowired
    private LlmApiKeyCipher llmApiKeyCipher;
    /**
     * 各厂商的轮询序号，key为厂商id
     */
    private final Map<Long, AtomicLong> providerSequences = new ConcurrentHashMap<>();
    /**
     * 当前节点的冷却记录，key为凭据id
     */
    private final Map<Long, ProviderKeyCooldown> localCooldowns = new ConcurrentHashMap<>();

    /**
     * 为单次物理调用选择可用凭据
     * @param providerId 供应商id
     * @return 选中凭据
     */
    public ProviderKeyRoute select(Long providerId) {
        if (providerId == null) {
            throw new IllegalArgumentException("providerId must not be null");
        }
        List<ProviderKeyCredential> credentials = providerKeyDao.queryAvailableProviderKeyList(providerId);
        List<ProviderKeyCredential> availableCredentials = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        if (!CollectionUtil.isEmpty(credentials)) {
            for (ProviderKeyCredential credential : credentials) {
                if (!this.isLocallyCooling(credential, currentTime)) {
                    availableCredentials.add(credential);
                }
            }
        }
        if (availableCredentials.isEmpty()) {
            String userMessage = ModelErrorTypeEnum.RATE_LIMIT.getUserMessage();
            throw new ModelInvocationException(ModelErrorTypeEnum.RATE_LIMIT, 429,
                    "no available providerKey", null, userMessage);
        }
        AtomicLong sequence = providerSequences.computeIfAbsent(providerId, id -> new AtomicLong());
        long sequenceNumber = sequence.getAndIncrement();
        int selectedIndex = (int) Math.floorMod(sequenceNumber, (long) availableCredentials.size());
        ProviderKeyCredential selected = availableCredentials.get(selectedIndex);
        String secretKey = llmApiKeyCipher.decrypt(selected.getSecretKey());
        Long providerKeyId = selected.getId();
        Long selectedProviderId = selected.getProviderId();
        String encryptedKey = selected.getSecretKey();
        return new ProviderKeyRoute(providerKeyId, selectedProviderId, encryptedKey, secretKey);
    }

    /**
     * 限流错误时冷却当前凭据
     * @param providerId 供应商id
     * @param providerKeyId 凭据id
     * @param error 模型调用异常
     */
    public void handleError(Long providerId, Long providerKeyId, Throwable error) {
        if (providerId == null || providerKeyId == null || error == null) {
            return;
        }
        ModelErrorTypeEnum errorType = ModelErrorClassifier.classify(error);
        if (errorType != ModelErrorTypeEnum.RATE_LIMIT) {
            return;
        }
        long localCooldownUntil = System.currentTimeMillis() + COOLDOWN_MS;
        ProviderKeyCooldown localCooldown = new ProviderKeyCooldown(providerId, localCooldownUntil);
        localCooldowns.merge(providerKeyId, localCooldown, (current, replacement) -> {
            if (current.getCooldownUntil() >= replacement.getCooldownUntil()) {
                return current;
            }
            return replacement;
        });
        try {
            Timestamp currentTime = providerKeyDao.queryCurrentTime();
            if (currentTime == null) {
                throw new IllegalStateException("database currentTime must not be null");
            }
            Timestamp cooldownUntil = new Timestamp(currentTime.getTime() + COOLDOWN_MS);
            ProviderKey providerKey = new ProviderKey();
            providerKey.setId(providerKeyId);
            providerKey.setCooldownUntil(cooldownUntil);
            int count = providerKeyDao.updateDBById(providerKey);
            if (count < 1) {
                logger.warn("providerKey cooldown update skipped, providerKeyId={}", providerKeyId);
            }
        } catch (RuntimeException e) {
            logger.warn("providerKey cooldown update error, providerKeyId={}", providerKeyId, e);
        }
    }

    /**
     * 清理供应商轮询游标
     * @param providerId 供应商id
     */
    public void clearProvider(Long providerId) {
        if (providerId == null) {
            return;
        }
        providerSequences.remove(providerId);
        localCooldowns.entrySet().removeIf(entry -> providerId.equals(entry.getValue().getProviderId()));
    }

    /**
     * 判断当前节点是否仍在凭据冷却期
     */
    private boolean isLocallyCooling(ProviderKeyCredential credential, long currentTime) {
        Long providerKeyId = credential.getId();
        ProviderKeyCooldown cooldown = localCooldowns.get(providerKeyId);
        if (cooldown == null) {
            return false;
        }
        if (cooldown.getCooldownUntil() <= currentTime) {
            localCooldowns.remove(providerKeyId, cooldown);
            return false;
        }
        return true;
    }
}
