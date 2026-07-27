import CryptoJS from 'crypto-js';

export function des(str) {
    const key = import.meta.env.VITE_ENCRYPT_PRIVATE_KEY;
    const keyHex = CryptoJS.enc.Utf8.parse(key);
    const encrypted = CryptoJS.DES.encrypt(str, keyHex, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7,
    });
    return encrypted.ciphertext.toString();
}
