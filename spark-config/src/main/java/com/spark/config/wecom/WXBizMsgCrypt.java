package com.spark.config.wecom;

import com.spark.bean.base.AESException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 10:02:00
 * 企业微信消息加解密核心类
 * 提供接收消息时的解密和回复消息时的加密功能
 */
public class WXBizMsgCrypt {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final String token;
    private final String encodingAesKey;
    private final String receiveId;

    /**
     * 构造函数
     * @param token          企业微信后台配置的 Token
     * @param encodingAesKey 企业微信后台配置的 EncodingAESKey
     * @param receiveId      企业微信的 CorpId（或第三方应用的 AppId）
     */
    public WXBizMsgCrypt(String token, String encodingAesKey, String receiveId) {
        this.token = token;
        this.encodingAesKey = encodingAesKey;
        this.receiveId = receiveId;
    }

    /**
     * 验证 URL 和密文的签名，验证通过后返回解密后的 echostr
     * 用于企业微信回调 URL 验证
     * @param msgSignature 签名串（msg_signature）
     * @param timeStamp    时间戳
     * @param nonce        随机数
     * @param echoStr      密文
     * @return 解密后的 echostr
     * @throws AESException 验证或解密失败时抛出
     */
    public String verifyUrl(String msgSignature, String timeStamp, String nonce, String echoStr) throws AESException {
        // 验证签名
        String signature = getSHA1(token, timeStamp, nonce, echoStr);
        if (!signature.equals(msgSignature)) {
            throw new AESException(AESException.ValidateSignatureError);
        }
        // 解密
        byte[] aesKey = Base64.getDecoder().decode(encodingAesKey);
        byte[] result = decrypt(aesKey, echoStr);
        // 提取消息内容（PKCS7Encoder 负责从结果中解析出实际消息内容）
        return extractMessage(result);
    }

    /**
     * 解密收到的密文
     * @param msgSignature 签名串
     * @param timeStamp    时间戳
     * @param nonce        随机数
     * @param postData     密文 XML
     * @return 解密后的原文
     * @throws AESException 验证或解密失败时抛出
     */
    public String decryptMsg(String msgSignature, String timeStamp, String nonce, String postData) throws AESException {
        // 从 XML 中提取加密密文
        String encrypt = extractEncryptMsg(postData);
        // 验证签名
        String signature = getSHA1(token, timeStamp, nonce, encrypt);
        if (!signature.equals(msgSignature)) {
            throw new AESException(AESException.ValidateSignatureError);
        }
        // 解密
        byte[] aesKey = Base64.getDecoder().decode(encodingAesKey);
        byte[] result = decrypt(aesKey, encrypt);
        // 提取消息内容
        String content = extractMessage(result);
        // 验证 receiveId
        byte[] contentBytes = decode(result);
        String corpIdFromMsg = new String(contentBytes, 20 + content.getBytes(CHARSET).length, receiveId.length(), CHARSET);
        if (!corpIdFromMsg.equals(receiveId)) {
            throw new AESException(AESException.ValidateCorpidError);
        }
        return content;
    }

    /**
     * 加密需要回复的消息
     * @param replyMsg 需要回复的明文消息
     * @param timeStamp 时间戳
     * @param nonce     随机数
     * @return 加密后的 XML 字符串
     * @throws AESException 加密失败时抛出
     */
    public String encryptMsg(String replyMsg, String timeStamp, String nonce) throws AESException {
        // 生成 16 位随机字符串
        String randomStr = randomString();
        // 构建网络字节序数据
        byte[] aesKey = Base64.getDecoder().decode(encodingAesKey);
        byte[] plainBytes = buildMessage(randomStr, replyMsg, receiveId);
        // AES 加密
        byte[] encrypted = encrypt(aesKey, plainBytes);
        String encryptedStr = Base64.getEncoder().encodeToString(encrypted);
        // 生成签名
        String signature = getSHA1(token, timeStamp, nonce, encryptedStr);
        // 生成 XML 回复
        return generateEncryptXml(encryptedStr, signature, timeStamp, nonce);
    }

    /**
     * AES 解密
     * @param aesKey  密钥
     * @param cipherText 密文（Base64 编码）
     * @return 解密后的字节数组
     * @throws AESException 解密失败时抛出
     */
    private byte[] decrypt(byte[] aesKey, String cipherText) throws AESException {
        try {
            byte[] encrypted = Base64.getDecoder().decode(cipherText);
            // 取密钥的前 16 字节作为 IV
            byte[] iv = Arrays.copyOfRange(aesKey, 0, 16);
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new AESException(AESException.DecryptAESError);
        }
    }

    /**
     * AES 加密
     * @param aesKey  密钥
     * @param plainBytes 明文字节数组
     * @return 加密后的字节数组
     * @throws AESException 加密失败时抛出
     */
    private byte[] encrypt(byte[] aesKey, byte[] plainBytes) throws AESException {
        try {
            byte[] iv = Arrays.copyOfRange(aesKey, 0, 16);
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(plainBytes);
        } catch (Exception e) {
            throw new AESException(AESException.EncryptAESError);
        }
    }

    /**
     * 生成 16 位随机字符串
     * @return 随机字符串
     */
    private String randomString() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 用 SHA1 算法生成安全签名
     * @param token     票据
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param encrypt   密文
     * @return 安全签名
     * @throws AESException 签名生成失败时抛出
     */
    public static String getSHA1(String token, String timestamp, String nonce, String encrypt) throws AESException {
        try {
            String[] array = new String[]{token, timestamp, nonce, encrypt};
            StringBuilder sb = new StringBuilder();
            // 字符串排序
            Arrays.sort(array);
            for (String s : array) {
                sb.append(s);
            }
            String str = sb.toString();
            // SHA1 签名生成
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(str.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexStr = new StringBuilder();
            for (byte b : digest) {
                hexStr.append(String.format("%02x", b & 0xFF));
            }
            return hexStr.toString();
        } catch (Exception e) {
            throw new AESException(AESException.ComputeSignatureError);
        }
    }


    /**
     * 从 xml 中提取加密消息
     * @param xmlText 待解析的 xml 字符串
     * @return 提取到的加密消息字符串
     * @throws AESException xml解析失败时抛出
     */
    public static String extractEncryptMsg(String xmlText) throws AESException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(xmlText);
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);
            Element root = document.getDocumentElement();
            NodeList nodelist1 = root.getElementsByTagName("Encrypt");
            return nodelist1.item(0).getTextContent();
        } catch (Exception e) {
            throw new AESException(AESException.ParseXmlError);
        }
    }

    /**
     * 生成加密回复的 xml 字符串
     * @param encrypt   加密后的消息
     * @param signature 安全签名
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @return 生成的 xml 字符串
     */
    public static String generateEncryptXml(String encrypt, String signature, String timestamp, String nonce) {
        String format = "<xml>\n<Encrypt><![CDATA[%1$s]]></Encrypt>\n<MsgSignature><![CDATA[%2$s]]></MsgSignature>\n<TimeStamp>%3$s</TimeStamp>\n<Nonce><![CDATA[%4$s]]></Nonce>\n</xml>";
        return String.format(format, encrypt, signature, timestamp, nonce);
    }


    private static final int BLOCK_SIZE = 32;

    /**
     * 对明文进行 PKCS7 补位
     * @param count 需要补位的数据长度
     * @return 补位后的字节数组
     */
    public static byte[] encode(int count) {
        int amountToPad = BLOCK_SIZE - (count % BLOCK_SIZE);
        if (amountToPad == 0) {
            amountToPad = BLOCK_SIZE;
        }
        byte[] padBytes = new byte[amountToPad];
        for (int i = 0; i < amountToPad; i++) {
            padBytes[i] = (byte) amountToPad;
        }
        return padBytes;
    }

    /**
     * 对解密后的数据进行 PKCS7 解码，去除补位字符
     * @param decrypted 解密后的数据
     * @return 去除补位后的数据
     */
    public static byte[] decode(byte[] decrypted) {
        int pad = decrypted[decrypted.length - 1];
        if (pad < 1 || pad > BLOCK_SIZE) {
            pad = 0;
        }
        if (pad > 0) {
            return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
        }
        return decrypted;
    }

    /**
     * 对解密后的网络字节序数据，提取实际消息内容
     * 数据格式：AESKey(16位随机) + 4字节网络字节序长度 + 实际消息内容 + 企业微信ID + PKCS7补位
     * @param decrypted 解密后的数据（含补位）
     * @return 实际消息内容
     * @throws AESException 数据非法时抛出
     */
    public static String extractMessage(byte[] decrypted) throws AESException {
        // 去除补位
        byte[] content = decode(decrypted);
        // 取 4 字节网络字节序长度
        ByteBuffer buffer = ByteBuffer.wrap(content, 16, 4);
        int msgLen = buffer.getInt();
        // 取实际消息内容（从 20 字节开始，长度 msgLen）
        return new String(content, 20, msgLen, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 构建加密前的网络字节序数据
     * @param randomStr 16 位随机字符串
     * @param message   实际消息内容
     * @param receiveId 企业微信 CorpId 或应用 APPId
     * @return 构建后的字节数组（含 PKCS7 补位）
     */
    public static byte[] buildMessage(String randomStr, String message, String receiveId) {
        ByteBuffer buffer = ByteBuffer.allocate(20 + message.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + receiveId.length());
        // 随机字符串
        buffer.put(randomStr.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // 4 字节网络字节序的消息长度
        buffer.putInt(message.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        // 消息内容
        buffer.put(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // 企业微信ID
        buffer.put(receiveId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // PKCS7 补位
        byte[] plainBytes = buffer.array();
        byte[] padBytes = encode(plainBytes.length);
        byte[] result = Arrays.copyOf(plainBytes, plainBytes.length + padBytes.length);
        System.arraycopy(padBytes, 0, result, plainBytes.length, padBytes.length);
        return result;
    }
}