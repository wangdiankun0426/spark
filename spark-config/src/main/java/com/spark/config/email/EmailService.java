package com.spark.config.email;

import com.spark.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/10 15:01
 */
@Component
public class EmailService {
    private final static Logger logger = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    private EmailConfig emailConfig;

    /**
     * 发送登录验证码
     * @param code
     * @param email
     * @return
     */
    public boolean sendLoginValidate(String email, String code) {
        logger.info("sendLoginValidate email={}, code={}", email, code);
        if (StringUtil.isBlank(email) ||  StringUtil.isBlank(code)) {
            return false;
        }
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", emailConfig.isEnableAuth());
            props.put("mail.smtp.host", emailConfig.getHost());
            // 构建授权信息，用于进行SMTP进行身份验证
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailConfig.getUser(), emailConfig.getPassword());
                }
            };
            // 使用环境属性和授权信息，创建邮件会话
            Session mailSession = Session.getInstance(props, authenticator);
            // 创建邮件消息
            MimeMessage message = new MimeMessage(mailSession);
            // 设置发件人
            InternetAddress form = new InternetAddress(emailConfig.getUser(), "星火云应用平台");
            message.setFrom(form);
            // 设置收件人
            InternetAddress toAddress = new InternetAddress(email);
            message.setRecipient(Message.RecipientType.TO, toAddress);
            String text = "您好，您正在使用星火云应用平台邮箱登录验证，验证码为"+code+"。2分钟有效，如若把验证码泄露他人，后果请自行承担！";
            String title = "登录验证码";
            // 设置邮件标题
            message.setSubject(title);
            // 设置邮件的内容体
            message.setContent(text, "text/html;charset=UTF-8");
            // 发送邮件
            Transport.send(message);
            return true;
        } catch (Exception e) {
            logger.error("sendLoginValidate error", e);
        }
        return false;
    }

//    public static void main(String[] args) {
//        EmailConfig.sendLoginValidate("1234", "1261413959@qq.com");
//    }
}
