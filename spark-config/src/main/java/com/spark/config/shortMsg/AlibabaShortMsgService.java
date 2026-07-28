package com.spark.config.shortMsg;

import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.teaopenapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/10 11:26
 * 阿里云短信服务
 */
@Component
public class AlibabaShortMsgService {
    private static final Logger logger = LoggerFactory.getLogger(AlibabaShortMsgService.class);
    @Autowired
    private AlibabaShortMsgConfig alibabaShortMsgConfig;
    private com.aliyun.dysmsapi20170525.Client client;

    /**
     * 发送登录验证码
     * @throws Exception
     */
    public boolean sendLoginValidate(String phone, String code) {
        if (client == null) {
            Config config = new Config();
            config.accessKeyId = alibabaShortMsgConfig.getAccessKeyId();
            config.accessKeySecret = alibabaShortMsgConfig.getAccessKeySecret();
            try {
                client =  new com.aliyun.dysmsapi20170525.Client(config);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 发送短信
        SendSmsRequest sendReq = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(alibabaShortMsgConfig.getSignName())
                .setTemplateCode(alibabaShortMsgConfig.getTemplateCode())
                .setTemplateParam("{code:"+code+"}");
        SendSmsResponse sendResp = null;
        try {
            sendResp = client.sendSms(sendReq);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("aliyun sendLoginValidate result is {}", sendResp.body);
        return "OK".equals(sendResp.body.message);
    }

}
