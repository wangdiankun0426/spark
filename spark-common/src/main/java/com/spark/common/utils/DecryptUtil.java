package com.spark.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/29 14:27
 * 解密工具类
 */
public class DecryptUtil {
    private static final Logger logger = LoggerFactory.getLogger(DecryptUtil.class);

    /**
     * des解密
     * @param src
     * @param key
     * @return
     */
    public static String des(String src, String key)  {
        try {
            //1.KEY转换 实例化DESKey秘钥的相关内容
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
            //实例一个秘钥工厂，指定加密方式
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            Key convertSecretKey = factory.generateSecret(desKeySpec);
            //2.解密  DES/ECB/PKCS5Padding--->算法/工作方式/填充方式 通过Cipher这个类进行加解密相关操作
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, convertSecretKey);
            byte[] encrypted = decodeHex(src.toCharArray());
            byte[] result = cipher.doFinal(encrypted);
            return new String(result, "UTF-8");
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 成16进制转字节
     * @param data
     * @return
     */
    private static byte[] decodeHex(char[] data) {
        int len = data.length;
        if ((len & 1) != 0) {
            logger.error("Hexadecimal string is of odd length.");
            return null;
        } else {
            byte[] out = new byte[len >> 1];
            int i = 0;

            for(int j = 0; j < len; ++i) {
                int f = toDigit(data[j], j) << 4;
                ++j;
                f |= toDigit(data[j], j);
                ++j;
                out[i] = (byte)(f & 255);
            }
            return out;
        }
    }

    /**
     * 16进制转字节
     * @param ch
     * @param index
     * @return
     */
    private static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            logger.error("Illegal hexadecimal character " + ch + " at index " + index);
            return -1;
        } else {
            return digit;
        }
    }

}
