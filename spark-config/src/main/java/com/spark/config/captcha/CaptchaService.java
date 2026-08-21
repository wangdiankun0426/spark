package com.spark.config.captcha;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

import static com.google.code.kaptcha.Constants.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/7 21:17
 * 验证码配置
 */
@Configuration
public class CaptchaService {

    /**
     * 验证码bean
     * @return
     */
    @Bean(name = "captchaProducer")
    public DefaultKaptcha getCaptchaBean() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 是否有边框 默认为true 我们可以自己设置yes，no
        properties.setProperty(KAPTCHA_BORDER, "yes");
        // 验证码文本字符颜色 设置为白色
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_COLOR, "white");
        // 验证码图片宽度 默认为200
        properties.setProperty(KAPTCHA_IMAGE_WIDTH, "250");
        // 验证码图片高度 默认为50
        properties.setProperty(KAPTCHA_IMAGE_HEIGHT, "100");
        // 验证码文本字符大小 默认为40
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_SIZE, "70");
        // KAPTCHA_SESSION_KEY
        properties.setProperty(KAPTCHA_SESSION_CONFIG_KEY, "kaptchaCode");
        // 验证码文本字符长度 默认为5
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        // 验证码文本字体样式 默认为new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
        properties.setProperty(KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier");
        // 图片背景颜色 设置为蓝色
        properties.setProperty(KAPTCHA_BACKGROUND_CLR_FROM, "0,0,255");
        properties.setProperty(KAPTCHA_BACKGROUND_CLR_TO, "0,0,255");
        // 字符间距 默认值为2，可以适当增大
        properties.setProperty(KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "10");
        // 图片样式 水纹com.google.code.kaptcha.executor.WaterRipple 鱼眼com.google.code.kaptcha.executor.FishEyeGimpy 阴影com.google.code.kaptcha.executor.ShadowGimpy
        //properties.setProperty(KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.executor.FishEyeGimpy");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
