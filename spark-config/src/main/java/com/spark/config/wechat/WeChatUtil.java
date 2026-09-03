package com.spark.config.wechat;

import com.spark.config.wechat.response.WeChatSessionRes;
import com.spark.utils.JsonUtil;
import com.spark.utils.StringUtil;
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
 * @since 2026-09-03 10:00:00
 * 微信工具类
 */
@Component
public class WeChatUtil {
    private final static Logger logger = LoggerFactory.getLogger(WeChatUtil.class);
    /**
     * 微信API基础地址
     */
    private static final String BASE_URL = "https://api.weixin.qq.com";
    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private WeChatConfig weChatConfig;

    /**
     * 微信登录code换openid
     * @param jsCode wechat.login返回的code
     * @return 登录会话
     */
    public WeChatSessionRes code2Session(String jsCode) {
        if (StringUtil.isBlank(jsCode) || StringUtil.isBlank(weChatConfig.getAppId()) || StringUtil.isBlank(weChatConfig.getAppSecret())) {
            logger.warn("code2Session skip, invalid param, appId or appSecret not configured");
            return null;
        }
        String url = BASE_URL + "/sns/jscode2session?appid={appid}&secret={secret}&js_code={js_code}&grant_type=authorization_code";
        // 微信接口返回的Content-Type为text/plain，直接映射POJO会因无匹配converter抛UnknownContentTypeException，先取原始字符串再解析
        String responseText = restTemplate.getForObject(url, String.class, weChatConfig.getAppId(), weChatConfig.getAppSecret(), jsCode);
        logger.info("code2Session responseText={}", responseText);
        if (StringUtil.isBlank(responseText)) {
            logger.error("code2Session error, response is blank");
            return null;
        }
        WeChatSessionRes response;
        try {
            response = JsonUtil.toObject(responseText, WeChatSessionRes.class);
        } catch (Exception e) {
            // 返回内容非JSON（如appid/secret配置异常或网络层拦截提示），记录原始报文便于排查
            logger.error("code2Session error, parse response fail, body={}", responseText, e);
            return null;
        }
        if (response == null || response.getErrcode() != null && response.getErrcode() != 0) {
            Integer errcode = response != null ? response.getErrcode() : null;
            String errmsg = response != null ? response.getErrmsg() : "response is null";
            logger.error("code2Session error, errcode={}, errmsg={}", errcode, errmsg);
            return null;
        }
        return response;
    }
}
