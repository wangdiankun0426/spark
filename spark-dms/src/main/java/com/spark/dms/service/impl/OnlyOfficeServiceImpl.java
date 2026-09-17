package com.spark.dms.service.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.utils.FileUtil;
import com.spark.common.utils.JsonUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.redis.RedisService;
import com.spark.dao.dms.DocumentDao;
import com.spark.dms.service.IDocumentVersionService;
import com.spark.dms.service.IOnlyOfficeService;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 15:00:00
 * OnlyOffice 在线编辑服务实现
 */
@Service
@LogPrint
public class OnlyOfficeServiceImpl extends BaseService<DocumentQuery, DocumentResult> implements IOnlyOfficeService {
    private final static Logger logger = LoggerFactory.getLogger(OnlyOfficeServiceImpl.class);
    /**
     * 下载凭证缓存key前缀
     */
    private final static String DOWNLOAD_TOKEN_KEY = "onlyoffice_token_";
    /**
     * 下载凭证有效期：10分钟（秒）
     */
    private final static long DOWNLOAD_TOKEN_EXPIRE_SECONDS = 10 * 60L;
    /**
     * 回调状态：文档已保存
     */
    private final static int CALLBACK_STATUS_SAVED = 2;
    /**
     * 回调状态：强制保存
     */
    private final static int CALLBACK_STATUS_FORCE_SAVED = 6;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private IDocumentVersionService documentVersionService;
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private RedisService redisService;
    @Value("${docs.file.path}")
    private String docsPath;
    @Value("${onlyoffice.url}")
    private String serverUrl;
    @Value("${onlyoffice.callback-host}")
    private String callbackHost;

    /**
     * 查询编辑器配置
     * @param id 文档id
     * @return 编辑器配置
     */
    @Override
    public ResultData<Map<String, Object>> queryEditorConfig(Long id) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        if (id == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        DocumentResult documentResult = this.queryDocument(id);
        if (documentResult == null) {
            result.setErrorCode(ErrorCodeEnum.DOCUMENT_NOT_EXIST);
            return result;
        }
        String ext = documentResult.getExt().toLowerCase();
        String documentType = this.resolveDocumentType(ext);
        if (documentType == null) {
            result.setErrorCode(ErrorCodeEnum.ONLYOFFICE_PREVIEW_NOT_SUPPORT);
            return result;
        }
        // 生成下载凭证，将文档信息与当前会话缓存10分钟，构造下载与回调地址
        String token = UUID.randomUUID().toString().replace("-", "");
        redisService.setStr(DOWNLOAD_TOKEN_KEY + token, JsonUtil.toString(this.buildTokenCache(documentResult)), DOWNLOAD_TOKEN_EXPIRE_SECONDS);
        String downloadUrl = callbackHost + "/dms/onlyoffice/download?id=" + id + "&ext=" + ext + "&token=" + token;
        String callbackUrl = callbackHost + "/dms/onlyoffice/callback?id=" + id + "&ext=" + ext + "&token=" + token;
        // 文档信息
        Map<String, Object> document = new HashMap<>();
        document.put("fileType", ext);
        document.put("key", id + "_" + documentResult.getVersionNo());
        document.put("title", documentResult.getName());
        document.put("url", downloadUrl);
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("edit", true);
        permissions.put("download", true);
        permissions.put("print", true);
        document.put("permissions", permissions);
        // 编辑器配置
        Map<String, Object> user = new HashMap<>();
        user.put("id", String.valueOf(SessionHolder.getCurrentUserId()));
        user.put("name", super.getObjName(SessionHolder.getCurrentUserId()));
        Map<String, Object> customization = new HashMap<>();
        customization.put("autosave", true);
        customization.put("forcesave", true);
        Map<String, Object> editorConfig = new HashMap<>();
        editorConfig.put("mode", "edit");
        editorConfig.put("lang", "zh-CN");
        editorConfig.put("callbackUrl", callbackUrl);
        editorConfig.put("user", user);
        editorConfig.put("customization", customization);
        // 编辑器完整配置
        Map<String, Object> config = new HashMap<>();
        config.put("documentType", documentType);
        config.put("document", document);
        config.put("editorConfig", editorConfig);
        config.put("type", "desktop");
        config.put("serverUrl", serverUrl);
        result.setData(config);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 下载编辑文件
     * @param id 文档id
     * @param ext 文件后缀
     * @param token 下载凭证
     * @return 文件信息
     */
    @Override
    public ResultData<Map<String, Object>> downloadFile(Long id, String ext, String token) {
        ResultData<Map<String, Object>> result = new ResultData<>();
        Map<String, Object> cacheData = this.parseTokenCache(id, ext, token);
        if (cacheData == null) {
            result.setErrorCode(ErrorCodeEnum.ONLYOFFICE_TOKEN_INVALID);
            return result;
        }
        Map<String, Object> documentCache = this.getCacheMap(cacheData, "document");
        String path = String.valueOf(documentCache.get("path"));
        File file = new File(path);
        if (!file.exists()) {
            logger.error("downloadFile file not exist, path={}", path);
            result.setErrorCode(ErrorCodeEnum.FILE_NOT_EXIST);
            return result;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("path", path);
        data.put("name", String.valueOf(documentCache.get("name")));
        result.setData(data);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 处理保存回调
     * @param id 文档id
     * @param ext 文件后缀
     * @param token 下载凭证
     * @param body 回调内容
     * @return 回调响应
     */
    @Override
    public Map<String, Object> handleCallback(Long id, String ext, String token, Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> cacheData = this.parseTokenCache(id, ext, token);
        if (cacheData == null) {
            response.put("error", 1);
            return response;
        }
        if (body == null || body.get("status") == null) {
            response.put("error", 0);
            return response;
        }
        int status = Integer.parseInt(String.valueOf(body.get("status")));
        // 保存完毕或强制保存时，拉回编辑后的文件生成新版本
        if (status == CALLBACK_STATUS_SAVED || status == CALLBACK_STATUS_FORCE_SAVED) {
            String fileUrl = (String) body.get("url");
            // 回调来自 OnlyOffice 服务器，无登录会话，从缓存还原编辑人会话供操作日志与审计字段使用
            try {
                this.restoreSession(cacheData);
                this.saveCallbackFile(id, ext, fileUrl);
            } finally {
                SessionHolder.clearLocalSession();
            }
        }
        response.put("error", 0);
        return response;
    }

    /**
     * 保存回调文件为文档新版本
     * @param id 文档id
     * @param ext 文件后缀
     * @param fileUrl 编辑后文件地址
     */
    private void saveCallbackFile(Long id, String ext, String fileUrl) {
        if (StringUtil.isBlank(fileUrl)) {
            logger.warn("saveCallbackFile skip, fileUrl blank, docId={}", id);
            return;
        }
        try {
            DocumentResult documentResult = this.queryDocument(id);
            if (documentResult == null) {
                logger.warn("saveCallbackFile skip, document not exist, docId={}", id);
                return;
            }
            byte[] bytes = this.downloadBytes(fileUrl);
            String newPath = fileUtil.generateFilePath(docsPath, UUID.randomUUID() + "." + ext);
            if (newPath == null) {
                logger.error("saveCallbackFile error, generate path fail, docId={}", id);
                return;
            }
            Files.write(new File(newPath).toPath(), bytes);
            Long size = (long) bytes.length;
            ResultData<Void> versionResult = documentVersionService.uploadVersion(id, documentResult.getName(), ext, size, newPath);
            if (versionResult.getCode() != ResultData.OK) {
                logger.error("saveCallbackFile error, upload version fail, docId={}, code={}", id, versionResult.getCode());
                return;
            }
            logger.info("saveCallbackFile success, docId={}, size={}", id, size);
        } catch (Exception e) {
            logger.error("saveCallbackFile error, docId={}", id, e);
        }
    }

    /**
     * 下载文件字节
     * @param fileUrl 文件地址
     * @return 文件字节
     */
    private byte[] downloadBytes(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(60000);
        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes();
        }
    }

    /**
     * 构造凭证缓存内容（文档信息 + 当前会话）
     * @param documentResult 文档
     * @return 缓存map
     */
    private Map<String, Object> buildTokenCache(DocumentResult documentResult) {
        Map<String, Object> cacheData = new HashMap<>();
        Map<String, Object> documentCache = new HashMap<>();
        documentCache.put("id", documentResult.getId());
        documentCache.put("ext", documentResult.getExt().toLowerCase());
        documentCache.put("name", documentResult.getName());
        documentCache.put("path", documentResult.getPath());
        cacheData.put("document", documentCache);
        Map<String, Object> sessionCache = new HashMap<>();
        sessionCache.put("userId", SessionHolder.getCurrentUserId());
        sessionCache.put("deptId", SessionHolder.getCurrentDeptId());
        sessionCache.put("tenantId", SessionHolder.getCurrentTenantId());
        cacheData.put("session", sessionCache);
        return cacheData;
    }

    /**
     * 校验下载凭证并取出缓存内容
     * @param id 文档id
     * @param ext 文件后缀
     * @param token 下载凭证
     * @return 校验通过返回缓存map（含 document/session），否则返回 null
     */
    private Map<String, Object> parseTokenCache(Long id, String ext, String token) {
        if (id == null || StringUtil.isBlank(ext) || StringUtil.isBlank(token)) {
            return null;
        }
        String cacheValue = redisService.getValue(DOWNLOAD_TOKEN_KEY + token);
        if (!JsonUtil.isValidJson(cacheValue)) {
            return null;
        }
        Map<String, Object> cacheData = JsonUtil.toObject(cacheValue, Map.class);
        Map<String, Object> documentCache = this.getCacheMap(cacheData, "document");
        if (documentCache == null) {
            return null;
        }
        if (!String.valueOf(id).equals(String.valueOf(documentCache.get("id"))) || !ext.toLowerCase().equals(String.valueOf(documentCache.get("ext")))) {
            return null;
        }
        return cacheData;
    }

    /**
     * 从缓存map中取出子map
     * @param cacheData 缓存map
     * @param key 键
     * @return 子map，不存在返回 null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getCacheMap(Map<String, Object> cacheData, String key) {
        Object value = cacheData.get(key);
        if (!(value instanceof Map)) {
            return null;
        }
        return (Map<String, Object>) value;
    }

    /**
     * 从缓存map中取出Long值
     * @param cacheData 缓存map
     * @param key 键
     * @return Long值，不存在返回 null
     */
    private Long getCacheLong(Map<String, Object> cacheData, String key) {
        Object value = cacheData.get(key);
        if (value == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    /**
     * 从缓存还原编辑人会话
     * @param cacheData 凭证缓存内容
     */
    private void restoreSession(Map<String, Object> cacheData) {
        Map<String, Object> sessionCache = this.getCacheMap(cacheData, "session");
        if (sessionCache == null) {
            return;
        }
        SessionHolder.setCurrentUserId(this.getCacheLong(sessionCache, "userId"));
        SessionHolder.setCurrentDeptId(this.getCacheLong(sessionCache, "deptId"));
        SessionHolder.setCurrentTenantId(this.getCacheLong(sessionCache, "tenantId"));
    }

    /**
     * 查询文档
     * @param id 文档id
     * @return 文档
     */
    private DocumentResult queryDocument(Long id) {
        if (!ObjectTypeEnum.DOCUMENT.equals(super.getObjEnum(id))) {
            return null;
        }
        DocumentQuery documentQuery = new DocumentQuery();
        documentQuery.setId(id);
        return documentDao.queryDocument(documentQuery);
    }

    /**
     * 解析 OnlyOffice 文档类型
     * @param ext 文件后缀
     * @return 文档类型，不支持返回 null
     */
    private String resolveDocumentType(String ext) {
        if ("doc".equals(ext) || "docx".equals(ext)) {
            return "word";
        }
        if ("xls".equals(ext) || "xlsx".equals(ext)) {
            return "cell";
        }
        if ("ppt".equals(ext) || "pptx".equals(ext)) {
            return "slide";
        }
        return null;
    }
}
