package com.spark.manage.si;

import com.spark.config.wecom.WeComConfig;
import com.spark.config.wecom.response.WeComDeptRes;
import com.spark.config.wecom.response.WeComTokenRes;
import com.spark.config.wecom.response.WeComUserRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 14:00:00
 * 企业微信 API 客户端
 */
@Component
public class WeComUtil {
    private final static Logger logger = LoggerFactory.getLogger(WeComUtil.class);
    /**
     * 企业微信API基础地址
     */
    private static final String BASE_URL = "https://qyapi.weixin.qq.com/cgi-bin";
    private final RestTemplate restTemplate = new RestTemplate();
    /**
     * 缓存的AccessToken
     */
    private String cachedAccessToken;
    /**
     * AccessToken过期时间戳（毫秒）
     */
    private long tokenExpireTime;
    @Autowired
    private WeComConfig weComConfig;

    /**
     * 获取企业微信AccessToken
     * 优先使用缓存，过期则重新获取
     * @return AccessToken
     */
    public String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedAccessToken;
        }
        String url = BASE_URL + "/gettoken?corpid={corpid}&corpsecret={corpsecret}";
        WeComTokenRes response = restTemplate.getForObject(url, WeComTokenRes.class,
                weComConfig.getCorpId(), weComConfig.getCorpSecret());
        if (response == null || response.getErrcode() != null && response.getErrcode() != 0) {
            String errMsg = response != null ? response.getErrmsg() : "response is null";
            logger.error("getAccessToken error, errcode={}, errmsg={}",
                    response != null ? response.getErrcode() : null, errMsg);
            throw new RuntimeException("获取企业微信AccessToken失败: " + errMsg);
        }
        cachedAccessToken = response.getAccessToken();
        // 提前5分钟过期，确保token有效
        tokenExpireTime = System.currentTimeMillis() + (response.getExpiresIn() - 300) * 1000L;
        logger.info("getAccessToken success, expiresIn={}", response.getExpiresIn());
        return cachedAccessToken;
    }

    /**
     * 获取企业微信部门列表
     * @param deptId 部门ID（不传或传0表示获取全量）
     * @return 部门列表响应
     */
    public WeComDeptRes getDepartmentList(Long deptId) {
        String token = getAccessToken();
        String url = BASE_URL + "/department/list?access_token={token}";
        if (deptId != null && deptId > 0) {
            url = url + "&id={id}";
            return restTemplate.getForObject(url, WeComDeptRes.class, token, deptId);
        }
        return restTemplate.getForObject(url, WeComDeptRes.class, token);
    }

    /**
     * 获取企业微信用户列表
     * @param deptId 部门ID
     * @return 用户列表响应
     */
    public WeComUserRes getUserList(Long deptId) {
        String token = getAccessToken();
        String url = BASE_URL + "/user/list?access_token={token}&department_id={deptId}";
        return restTemplate.getForObject(url, WeComUserRes.class, token, deptId);
    }
}