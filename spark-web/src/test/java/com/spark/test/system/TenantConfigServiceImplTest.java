package com.spark.test.system;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.bean.sys.entity.TenantConfig;
import com.spark.common.bean.sys.query.TenantConfigQuery;
import com.spark.common.bean.sys.result.TenantConfigResult;
import com.spark.common.bean.sys.vo.TenantConfigVO;
import com.spark.common.enums.AccountTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.TenantConfigEnum;
import com.spark.dao.sys.TenantConfigDao;
import com.spark.manage.sys.impl.TenantConfigServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 16:50:00
 * 租户配置服务map缓存与写失效测试
 */
public class TenantConfigServiceImplTest {
    private TenantConfigServiceImpl tenantConfigService;
    private TenantConfigDao tenantConfigDao;

    @BeforeEach
    void setUp() {
        tenantConfigService = new TenantConfigServiceImpl();
        tenantConfigDao = mock(TenantConfigDao.class);
        ReflectionTestUtils.setField(tenantConfigService, "tenantConfigDao", tenantConfigDao);
        Session session = new Session();
        session.setUserId(101L);
        session.setTenantId(0L);
        session.setAccountType(AccountTypeEnum.SITE_ADMIN.getValue());
        SessionHolder.initLocalSession(session);
    }

    @AfterEach
    void tearDown() {
        SessionHolder.clearLocalSession();
    }

    /**
     * 验证map查询首次落库后命中缓存不再查库，返回防御性副本被外部修改也不污染缓存
     */
    @Test
    void shouldCacheConfigMapAfterFirstQuery() {
        TenantConfigResult configRow = this.createConfigRow(1L, 103L, "llm.rpm", "5");
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of(configRow));
        ResultData<Map<String, String>> first = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, first.getCode());
        Map<String, String> firstMap = first.getData();
        firstMap.put("llm.rpm", "999");
        ResultData<Map<String, String>> second = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, second.getCode());
        Map<String, String> secondMap = second.getData();
        assertEquals("5", secondMap.get("llm.rpm"));
        verify(tenantConfigDao, times(1)).queryTenantConfigList(any(TenantConfigQuery.class));
    }

    /**
     * 验证修改配置成功后缓存立即失效，下一次map查询重新落库并读到新值
     */
    @Test
    void shouldReadNewValueAfterUpdate() {
        TenantConfigResult oldRow = this.createConfigRow(1L, 103L, "llm.rpm", "5");
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of(oldRow));
        ResultData<Map<String, String>> before = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, before.getCode());
        when(tenantConfigDao.queryTenantConfig(any(TenantConfigQuery.class))).thenReturn(oldRow);
        when(tenantConfigDao.updateDBById(any(TenantConfig.class))).thenReturn(1);
        TenantConfigVO tenantConfigVO = new TenantConfigVO();
        tenantConfigVO.setId(1L);
        tenantConfigVO.setTenantId(103L);
        tenantConfigVO.setName("模型每分钟请求数");
        tenantConfigVO.setKey("llm.rpm");
        tenantConfigVO.setValue("9");
        ResultData<Void> updateResult = tenantConfigService.updateTenantConfig(tenantConfigVO);
        assertEquals(ResultData.OK, updateResult.getCode());
        TenantConfigResult newRow = this.createConfigRow(1L, 103L, "llm.rpm", "9");
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of(newRow));
        ResultData<Map<String, String>> after = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, after.getCode());
        Map<String, String> afterMap = after.getData();
        assertEquals("9", afterMap.get("llm.rpm"));
        verify(tenantConfigDao, times(2)).queryTenantConfigList(any(TenantConfigQuery.class));
    }

    /**
     * 验证删除配置成功后缓存立即失效，删空后map查询返回不存在且空结果同样缓存防穿透
     */
    @Test
    void shouldInvalidateCacheAfterDelete() {
        TenantConfigResult configRow = this.createConfigRow(1L, 103L, "llm.rpm", "5");
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of(configRow));
        ResultData<Map<String, String>> before = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, before.getCode());
        when(tenantConfigDao.queryTenantConfig(any(TenantConfigQuery.class))).thenReturn(configRow);
        when(tenantConfigDao.deleteDBById(any(TenantConfig.class))).thenReturn(1);
        TenantConfigVO tenantConfigVO = new TenantConfigVO();
        tenantConfigVO.setId(1L);
        ResultData<Void> deleteResult = tenantConfigService.deleteTenantConfig(tenantConfigVO);
        assertEquals(ResultData.OK, deleteResult.getCode());
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of());
        ResultData<Map<String, String>> after = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ErrorCodeEnum.TENANT_CONFIG_NOT_EXIST.getValue(), after.getCode());
        ResultData<Map<String, String>> again = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ErrorCodeEnum.TENANT_CONFIG_NOT_EXIST.getValue(), again.getCode());
        verify(tenantConfigDao, times(2)).queryTenantConfigList(any(TenantConfigQuery.class));
    }

    /**
     * 验证创建配置成功后缓存立即失效，下一次map查询包含新建的key
     */
    @Test
    void shouldInvalidateCacheAfterCreate() {
        TenantConfigResult existRow = this.createConfigRow(1L, 103L, "llm.rpm", "5");
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of(existRow));
        ResultData<Map<String, String>> before = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, before.getCode());
        when(tenantConfigDao.queryTenantConfig(any(TenantConfigQuery.class))).thenReturn(null);
        when(tenantConfigDao.insertDB(any(TenantConfig.class))).thenAnswer(invocation -> {
            TenantConfig tenantConfig = invocation.getArgument(0);
            tenantConfig.setId(2L);
            return 1;
        });
        TenantConfigVO tenantConfigVO = new TenantConfigVO();
        tenantConfigVO.setTenantId(103L);
        tenantConfigVO.setName("模型每分钟token数");
        tenantConfigVO.setKey("llm.tpm");
        tenantConfigVO.setValue("1000");
        ResultData<Void> createResult = tenantConfigService.createTenantConfig(tenantConfigVO);
        assertEquals(ResultData.OK, createResult.getCode());
        TenantConfigResult newRow = this.createConfigRow(2L, 103L, "llm.tpm", "1000");
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of(existRow, newRow));
        ResultData<Map<String, String>> after = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, after.getCode());
        Map<String, String> afterMap = after.getData();
        assertTrue(afterMap.containsKey("llm.tpm"));
        verify(tenantConfigDao, times(2)).queryTenantConfigList(any(TenantConfigQuery.class));
    }

    /**
     * 验证默认配置创建按枚举全量种子并失效缓存，空缓存租户创建后下一次map查询立即读到默认配置
     */
    @Test
    void shouldInvalidateCacheAfterCreateDefaultConfig() {
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of());
        ResultData<Map<String, String>> before = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ErrorCodeEnum.TENANT_CONFIG_NOT_EXIST.getValue(), before.getCode());
        when(tenantConfigDao.insertDB(any(TenantConfig.class))).thenReturn(1);
        ResultData<Integer> createResult = tenantConfigService.createDefaultConfig(103L);
        assertEquals(ResultData.OK, createResult.getCode());
        int enumCount = TenantConfigEnum.values().length;
        assertEquals(enumCount, createResult.getData());
        verify(tenantConfigDao, times(enumCount)).insertDB(any(TenantConfig.class));
        TenantConfigResult defaultRow = this.createConfigRow(1L, 103L, "llm.rpm", "0");
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenReturn(List.of(defaultRow));
        ResultData<Map<String, String>> after = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, after.getCode());
        verify(tenantConfigDao, times(2)).queryTenantConfigList(any(TenantConfigQuery.class));
    }

    /**
     * 验证系统管理员把配置从103租户迁移到104租户时两个租户缓存同时失效，各自下一次map查询都重新落库
     */
    @Test
    void shouldInvalidateBothTenantsWhenSysAdminMigratesConfig() {
        TenantConfigResult rowA = this.createConfigRow(1L, 103L, "llm.rpm", "5");
        TenantConfigResult rowB = this.createConfigRow(2L, 104L, "llm.tpm", "1000");
        Map<Long, List<TenantConfigResult>> dataStore = new HashMap<>();
        dataStore.put(103L, List.of(rowA));
        dataStore.put(104L, List.of(rowB));
        when(tenantConfigDao.queryTenantConfigList(any(TenantConfigQuery.class))).thenAnswer(invocation -> {
            TenantConfigQuery query = invocation.getArgument(0);
            Long tenantId = query.getTenantId();
            return dataStore.get(tenantId);
        });
        ResultData<Map<String, String>> before103 = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ResultData.OK, before103.getCode());
        ResultData<Map<String, String>> before104 = tenantConfigService.queryTenantConfigMap(104L);
        assertEquals(ResultData.OK, before104.getCode());
        when(tenantConfigDao.queryTenantConfig(any(TenantConfigQuery.class))).thenReturn(rowA);
        when(tenantConfigDao.updateDBById(any(TenantConfig.class))).thenReturn(1);
        TenantConfigVO tenantConfigVO = new TenantConfigVO();
        tenantConfigVO.setId(1L);
        tenantConfigVO.setTenantId(104L);
        tenantConfigVO.setName("模型每分钟请求数");
        tenantConfigVO.setKey("llm.rpm");
        tenantConfigVO.setValue("5");
        ResultData<Void> updateResult = tenantConfigService.updateTenantConfig(tenantConfigVO);
        assertEquals(ResultData.OK, updateResult.getCode());
        TenantConfigResult movedRow = this.createConfigRow(1L, 104L, "llm.rpm", "5");
        dataStore.put(103L, List.of());
        dataStore.put(104L, List.of(movedRow, rowB));
        ResultData<Map<String, String>> after103 = tenantConfigService.queryTenantConfigMap(103L);
        assertEquals(ErrorCodeEnum.TENANT_CONFIG_NOT_EXIST.getValue(), after103.getCode());
        ResultData<Map<String, String>> after104 = tenantConfigService.queryTenantConfigMap(104L);
        assertEquals(ResultData.OK, after104.getCode());
        Map<String, String> after104Map = after104.getData();
        assertTrue(after104Map.containsKey("llm.rpm"));
        assertFalse(after104Map.containsKey("llm.max.concurrent"));
        verify(tenantConfigDao, times(4)).queryTenantConfigList(any(TenantConfigQuery.class));
    }

    /**
     * 构造租户配置行
     */
    private TenantConfigResult createConfigRow(Long id, Long tenantId, String key, String value) {
        TenantConfigResult configRow = new TenantConfigResult();
        configRow.setId(id);
        configRow.setTenantId(tenantId);
        configRow.setKey(key);
        configRow.setValue(value);
        return configRow;
    }
}