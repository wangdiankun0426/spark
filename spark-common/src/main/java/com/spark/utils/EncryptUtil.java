package com.spark.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/29 14:26
 * 加密工具类
 */
public class EncryptUtil {
    /**
     * 登录密码加密
     * @param origin
     * @return
     * @throws Exception
     */
    public static String md5(String origin) {
        if( StringUtil.isBlank(origin)){
            return null;
        }
        try{
            byte[] buf = origin.getBytes();
            MessageDigest algorithm = null;
            try {
                algorithm = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            algorithm.reset();
            algorithm.update(buf);
            byte[] digest = algorithm.digest();
            return new String(encodeHex(digest));
        }catch (Exception e){
            return null;
        }
    }

    /**
     * des加密
     * @param src 源值
     * @param key 8位
     * @return
     */
    public static String des(String src, String key)  {
        try{
            //1.KEY转换 实例化DESKey秘钥的相关内容
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
            //实例一个秘钥工厂，指定加密方式
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
            Key convertSecretKey = factory.generateSecret(desKeySpec);
            //2.加密 DES/ECB/PKCS5Padding--->算法/工作方式/填充方式 通过Cipher这个类进行加解密相关操作
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, convertSecretKey);
            //输入要加密的内容
            byte[] result = cipher.doFinal(src.getBytes("UTF-8"));
            return new String(encodeHex(result));
        } catch (Exception e){
            return null;
        }
    }

    /**
     * 字节转成16进制
     * @param data
     * @return
     */
    private static char[] encodeHex(byte[] data) {
        char[] toDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;
        for(int var5 = 0; i < l; ++i) {
            out[var5++] = toDigits[(240 & data[i]) >>> 4];
            out[var5++] = toDigits[15 & data[i]];
        }
        return out;
    }

}
