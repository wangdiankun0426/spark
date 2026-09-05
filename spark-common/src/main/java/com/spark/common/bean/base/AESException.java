package com.spark.common.bean.base;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 10:02:00
 * 企业微信加解密异常类
 */
@SuppressWarnings("serial")
public class AESException extends Exception {

    public final static int OK = 0;
    public final static int ValidateSignatureError = -40001;
    public final static int ParseXmlError = -40002;
    public final static int ComputeSignatureError = -40003;
    public final static int IllegalAesKey = -40004;
    public final static int ValidateCorpidError = -40005;
    public final static int EncryptAESError = -40006;
    public final static int DecryptAESError = -40007;
    public final static int IllegalBuffer = -40008;

    private int code;

    public AESException(int code) {
        super(getMessage(code));
        this.code = code;
    }

    private static String getMessage(int code) {
        return switch (code) {
            case ValidateSignatureError -> "签名验证错误";
            case ParseXmlError -> "xml解析失败";
            case ComputeSignatureError -> "sha加密生成签名失败";
            case IllegalAesKey -> "SymmetricKey非法";
            case ValidateCorpidError -> "corpid校验失败";
            case EncryptAESError -> "aes加密失败";
            case DecryptAESError -> "aes解密失败";
            case IllegalBuffer -> "解密后得到的buffer非法";
            default -> "未知异常";
        };
    }

    public int getCode() {
        return code;
    }
}