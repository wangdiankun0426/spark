package com.spark.workflow.engine.executor;

import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.WorkflowTemplateTypeEnum;
import com.spark.manage.BaseService;
import com.spark.utils.JsonUtil;
import com.spark.utils.MapUtil;
import com.spark.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-26 18:00:00
 * HTTP请求节点执行器：支持调用外部API
 */
@Component
public class HttpRequestExecutor extends BaseService implements IWfNodeExecutor {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestExecutor.class);

    /**
     * 节点类型
     * @return
     */
    @Override
    public String getNodeType() {
        return WorkflowTemplateTypeEnum.HTTP_REQUEST.getValue();
    }

    /**
     * 执行节点
     * @param nodeId 节点id
     * @param config 节点配置
     * @param input 当前节点输入
     * @param valueMap 表单值
     * @param showValueMap 表单显示值
     * @return 节点输出
     */
    @Override
    public Map<String, String> execute(String nodeId, Map<String, Object> config, Map<String, String> input, Map<String, String> valueMap, Map<String, String> showValueMap) {
        Map<String, String> output = new HashMap<>();
        if (config == null) {
            throw new IllegalArgumentException(ErrorCodeEnum.INVALID_PARAM.getDesc());
        }
        String urlCode = MapUtil.getStringVal(config, "url");
        String url = super.generateFlowValue(urlCode, null, valueMap, showValueMap, input);
        if (StringUtil.isBlank(url)) {
            throw new IllegalArgumentException("HTTP请求节点未配置URL");
        }
        String method = MapUtil.getStringVal(config, "method", "GET").toUpperCase();
        Map<String, String> headers = new HashMap<>();
        String headersJson = MapUtil.getStringVal(config, "headers");
        if (StringUtil.isNotBlank(headersJson)) {
            String resolvedHeaders = super.generateFlowValue(headersJson, null, valueMap, showValueMap, input);
            if (JsonUtil.isValidJson(resolvedHeaders)) {
                Map<String, String> headerMap = JsonUtil.toObject(resolvedHeaders, Map.class);
                if (headerMap != null) {
                    headers.putAll(headerMap);
                }
            }
        }
        String body = null;
        if (!"GET".equals(method)) {
            String bodyCode = MapUtil.getStringVal(config, "body");
            if (StringUtil.isNotBlank(bodyCode)) {
                body = super.generateFlowValue(bodyCode, null, valueMap, showValueMap, input);
            }
        }
        int httpTimeoutMs = MapUtil.getIntegerVal(config, "httpTimeoutMs", 30000);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(httpTimeoutMs);
        factory.setReadTimeout(httpTimeoutMs);
        RestTemplate restTemplate = new RestTemplate(factory);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpHeaders.set(entry.getKey(), entry.getValue());
            }
            if (!httpHeaders.containsKey("Content-Type") && StringUtil.isNotBlank(body)) {
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            }
            HttpEntity<String> requestEntity = new HttpEntity<>(body, httpHeaders);
            ResponseEntity<String> responseEntity = switch (method) {
                case "GET" -> restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
                case "POST" -> restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
                case "PUT" -> restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
                case "DELETE" -> restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
                case "PATCH" -> restTemplate.exchange(url, HttpMethod.PATCH, requestEntity, String.class);
                default -> throw new IllegalArgumentException("不支持的HTTP方法: " + method);
            };
            String responseBody = responseEntity.getBody();
            int statusCode = responseEntity.getStatusCodeValue();
            output.put(nodeId + "statusCode", String.valueOf(statusCode));
            output.put(nodeId + "body", responseBody != null ? responseBody : "");
            output.put(nodeId + "result", responseBody != null ? responseBody : "");
            if (StringUtil.isNotBlank(responseBody) && JsonUtil.isValidJson(responseBody)) {
                Map<String, Object> jsonMap = JsonUtil.toObject(responseBody, Map.class);
                if (jsonMap != null) {
                    for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                        output.put(nodeId + "json." + entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
            }
            logger.info("HTTP node executed, method={}, url={}, statusCode={}", method, url, statusCode);
        } catch (Exception e) {
            logger.error("HTTP request failed, method={}, url={}", method, url, e);
            throw new RuntimeException("HTTP请求失败: " + e.getMessage(), e);
        }
        return output;
    }
}
