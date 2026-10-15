package com.spark.llm.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.McpKey;
import com.spark.common.bean.llm.query.McpKeyQuery;
import com.spark.common.bean.llm.result.McpKeyResult;
import com.spark.common.bean.llm.vo.McpKeyVO;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.redis.RedisService;
import com.spark.dao.llm.McpKeyDao;
import com.spark.llm.service.IMcpKeyService;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-15 14:10:00
 */
@Service
@LogPrint
public class McpKeyServiceImpl extends BaseService<McpKeyQuery, McpKeyResult> implements IMcpKeyService {
    private final static Logger logger = LoggerFactory.getLogger(McpKeyServiceImpl.class);
    /**
     * 密钥信息缓存秒数
     */
    private final static long KEY_CACHE_SECONDS = 300;
    /**
     * 默认密钥名称
     */
    private final static String DEFAULT_KEY_NAME = "个人MCP密钥";
    @Autowired
    private McpKeyDao mcpKeyDao;
    @Autowired
    private RedisService redisService;

    /**
     * 申请MCP密钥
     * @param mcpKeyVO MCP密钥数据
     * @return 创建结果，data为生成的完整密钥
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MCP_KEY_INSERT)
    public ResultData<String> createMcpKey(McpKeyVO mcpKeyVO) {
        ResultData<String> result = new ResultData<>();
        Long currentUserId = SessionHolder.getCurrentUserId();
        if (currentUserId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        McpKey mcpKey = new McpKey();
        if (mcpKeyVO != null) {
            BeanUtil.copyProperties(mcpKeyVO, mcpKey);
            if (StringUtil.isNotBlank(mcpKeyVO.getExpireDt())) {
                mcpKey.setExpireDt(Timestamp.valueOf(mcpKeyVO.getExpireDt()));
            }
        }
        mcpKey.setKeyValue(this.generateKeyValue());
        mcpKey.setUserId(currentUserId);
        if (StringUtil.isBlank(mcpKey.getName())) {
            mcpKey.setName(DEFAULT_KEY_NAME);
        }
        if (mcpKey.getStatus() == null) {
            mcpKey.setStatus(StatusEnum.NORMAL.getValue());
        }
        mcpKey.setTenantId(SessionHolder.getCurrentTenantId());
        int count = mcpKeyDao.insertDB(mcpKey);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        logger.info("createMcpKey success, mcpKeyId={}, userId={}", mcpKey.getId(), currentUserId);
        result.setObjId(mcpKey.getId());
        result.setData(mcpKey.getKeyValue());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除MCP密钥
     * @param mcpKeyVO MCP密钥数据
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MCP_KEY_DELETE)
    public ResultData<Void> deleteMcpKey(McpKeyVO mcpKeyVO) {
        ResultData<Void> result = new ResultData<>();
        if (mcpKeyVO == null || mcpKeyVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        McpKeyQuery query = new McpKeyQuery();
        query.setId(mcpKeyVO.getId());
        McpKeyResult existResult = mcpKeyDao.queryMcpKey(query);
        if (existResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        McpKey mcpKey = new McpKey();
        mcpKey.setId(mcpKeyVO.getId());
        int count = mcpKeyDao.deleteDBById(mcpKey);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        // 删除后清除密钥缓存
        redisService.del(ObjectCacheKey.MCP_API_KEY + existResult.getKeyValue());
        result.setObjId(mcpKeyVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询当前用户的MCP密钥列表
     * @return 密钥列表
     */
    @Override
    public ResultData<List<McpKeyResult>> queryMyKeyList() {
        ResultData<List<McpKeyResult>> result = new ResultData<>();
        Long currentUserId = SessionHolder.getCurrentUserId();
        if (currentUserId == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        McpKeyQuery query = new McpKeyQuery();
        query.setUserId(currentUserId);
        query.setPage(false);
        List<McpKeyResult> list = mcpKeyDao.queryMcpKeyList(query);
        if (CollectionUtil.isNotEmpty(list)) {
            for (McpKeyResult item : list) {
                supplySingle(item);
            }
        }
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验API密钥有效性
     * @param keyValue API密钥
     * @return 校验结果，data为密钥信息
     */
    @Override
    public ResultData<McpKeyResult> validateApiKey(String keyValue) {
        ResultData<McpKeyResult> result = new ResultData<>();
        if (StringUtil.isBlank(keyValue)) {
            result.setErrorCode(ErrorCodeEnum.MCP_KEY_INVALID);
            return result;
        }
        McpKeyResult mcpKey = null;
        String cacheKey = ObjectCacheKey.MCP_API_KEY + keyValue;
        String cacheJson = redisService.getValue(cacheKey);
        if (StringUtil.isNotBlank(cacheJson)) {
            mcpKey = JsonUtil.toObject(cacheJson, McpKeyResult.class);
        }
        if (mcpKey == null) {
            McpKeyQuery query = new McpKeyQuery();
            query.setKeyValue(keyValue);
            mcpKey = mcpKeyDao.queryMcpKey(query);
            if (mcpKey == null) {
                result.setErrorCode(ErrorCodeEnum.MCP_KEY_INVALID);
                return result;
            }
            // 刷新最近使用时间并写入缓存
            McpKey update = new McpKey();
            update.setId(mcpKey.getId());
            update.setLastUsedDt(new Timestamp(System.currentTimeMillis()));
            mcpKeyDao.updateDBById(update);
            redisService.setObj(cacheKey, JsonUtil.toString(mcpKey));
            redisService.expire(cacheKey, KEY_CACHE_SECONDS);
        }
        if (!StatusEnum.NORMAL.getValue().equals(mcpKey.getStatus())) {
            result.setErrorCode(ErrorCodeEnum.MCP_KEY_INVALID);
            return result;
        }
        if (mcpKey.getExpireDt() != null && mcpKey.getExpireDt().before(new Timestamp(System.currentTimeMillis()))) {
            result.setErrorCode(ErrorCodeEnum.MCP_KEY_INVALID);
            return result;
        }
        result.setData(mcpKey);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 生成API密钥
     * @return 密钥
     */
    private String generateKeyValue() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "sk-" + uuid;
    }

    /**
     * 补充单条数据
     * @param mcpKeyResult 数据
     */
    private void supplySingle(McpKeyResult mcpKeyResult) {
        mcpKeyResult.setStatusName(StatusEnum.indexOf(mcpKeyResult.getStatus()).getDesc());
        String userName = super.getObjName(mcpKeyResult.getUserId());
        mcpKeyResult.setUserName(userName);
    }
}
