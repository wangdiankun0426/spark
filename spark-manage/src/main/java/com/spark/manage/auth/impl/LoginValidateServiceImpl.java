package com.spark.manage.auth.impl;

import com.google.code.kaptcha.Producer;
import com.spark.common.bean.sys.vo.LoginVO;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.ValidateCode;
import com.spark.config.shortMsg.AlibabaShortMsgService;
import com.spark.config.email.EmailService;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.config.redis.RedisService;
import com.spark.common.enums.LoginTypeEnum;
import com.spark.manage.auth.ILoginValidateService;
import com.spark.common.utils.StringUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/7 20:29
 */
@Service
public class LoginValidateServiceImpl implements ILoginValidateService {
    private final static Logger logger = LoggerFactory.getLogger(LoginValidateServiceImpl.class);
    @Autowired
    private RedisService redisService;
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AlibabaShortMsgService alibabaShortMsgService;

    /**
     * 构造验证码
     * @return 构造结果
     */
    @Override
    public ResultData<ValidateCode> generateValidateCode() {
        ResultData<ValidateCode> result = new ResultData<>();
        String uuid = UUID.randomUUID().toString();
        String verifyKey = ObjectCacheKey.VALIDATE_CODE_KEY + uuid;
        String verifyCode;
        BufferedImage image;
        verifyCode = captchaProducer.createText();
        image = captchaProducer.createImage(verifyCode);
        redisService.setStr(verifyKey, verifyCode, 2*60);
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (Exception e) {
            logger.error("generateValidateCode error, e is", e);
        }
        ValidateCode validateCode = new ValidateCode();
        validateCode.setUuid(uuid);
        validateCode.setImg(Base64.getMimeEncoder().encodeToString(os.toByteArray()));
        result.setData(validateCode);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 构造短信验证码
     * @param loginVO 登录信息
     * @return 构造结果
     */
    @Override
    public ResultData<ValidateCode> generateSmsCode(LoginVO loginVO) {
        ResultData<ValidateCode> result = new ResultData<>();
        if (loginVO == null || StringUtil.isBlank(loginVO.getPhone())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String uuid = UUID.randomUUID().toString();
        String verifyKey = ObjectCacheKey.VALIDATE_CODE_KEY + uuid;
        String verifyCode = String.valueOf(new Random().nextInt(9000) + 1000);
        boolean bo = alibabaShortMsgService.sendLoginValidate(loginVO.getPhone(), verifyCode);
        if (!bo) {
            result.setErrorCode(ErrorCodeEnum.SMS_CODE_SEND_FAIL);
            return result;
        }
        redisService.setStr(verifyKey, verifyCode, 2*60);
        ValidateCode validateCode = new ValidateCode();
        validateCode.setUuid(uuid);
        result.setData(validateCode);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 获取邮箱验证码
     * @param loginVO 登录信息
     * @return 构造结果
     */
    @Override
    public ResultData<ValidateCode> generateEmailCode(LoginVO loginVO) {
        ResultData<ValidateCode> result = new ResultData<>();
        if (loginVO == null || StringUtil.isBlank(loginVO.getEmail())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String uuid = UUID.randomUUID().toString();
        String verifyKey = ObjectCacheKey.VALIDATE_CODE_KEY + uuid;
        String verifyCode = String.valueOf(new Random().nextInt(9000) + 1000);
        boolean bo = emailService.sendLoginValidate(loginVO.getEmail(), verifyCode);
        if (!bo) {
            result.setErrorCode(ErrorCodeEnum.SMS_CODE_SEND_FAIL);
            return result;
        }
        redisService.setStr(verifyKey, verifyCode, 2*60);
        ValidateCode validateCode = new ValidateCode();
        validateCode.setUuid(uuid);
        result.setData(validateCode);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验验证码
     * @param code 验证码
     * @return 校验结果
     */
    @Override
    public ResultData<Void> checkValidateCode(ValidateCode code) {
        ResultData<Void> result = new ResultData<>();
        if (code == null || code.getLoginType() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Integer loginType = code.getLoginType();
        if (!LoginTypeEnum.PASSWORD.getValue().equals(loginType) && !LoginTypeEnum.MESSAGE.getValue().equals(loginType)  && !LoginTypeEnum.EMAIL.getValue().equals(loginType) ) {
            result.setCode(ResultData.OK);
            return result;
        }
        if (StringUtil.isBlank(code.getUuid()) || StringUtil.isBlank(code.getValue())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String verifyKey = ObjectCacheKey.VALIDATE_CODE_KEY + code.getUuid();
        String value = redisService.getValue(verifyKey);
        if (StringUtil.isBlank(value)) {
            result.setErrorCode(ErrorCodeEnum.VALIDATE_INVALID);
            return result;
        }
        if (!value.equals(code.getValue())) {
            result.setErrorCode(ErrorCodeEnum.VALIDATE_CHECK_ERROR);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

}
