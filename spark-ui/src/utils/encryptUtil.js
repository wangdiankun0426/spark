import CryptoJS from 'crypto-js';

/**
 * DES加密（ECB + Pkcs7）
 * @param str 待加密内容
 * @param key 服务端下发的一次性密钥，固定8位ASCII字符
 * @returns {string} 16进制密文
 */
export function des(str, key) {
    const keyHex = CryptoJS.enc.Utf8.parse(key);
    const encrypted = CryptoJS.DES.encrypt(str, keyHex, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7,
    });
    return encrypted.ciphertext.toString();
}
