import CryptoJS from 'crypto-js';

// 加密密钥 (理想情况下应存储在环境变量中)
const SECRET_KEY = 'A1B2C3D4E5F6G7H8';

/**
 * 加密密码
 * @param password 明文密码
 * @returns 加密后的密码
 */
export const encryptPassword = (password: string): string => {
  try {
    // 使用AES加密算法，指定ECB模式和PKCS5Padding填充
    const key = CryptoJS.enc.Utf8.parse(SECRET_KEY);
    const encrypted = CryptoJS.AES.encrypt(password, key, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    });
    // 返回Base64编码的密文
    return encrypted.ciphertext.toString(CryptoJS.enc.Base64);
  } catch (error) {
    console.error('密码加密失败:', error);
    return password; // 如果加密失败，返回原密码
  }
};

/**
 * 解密密码 (通常不需要在前端解密)
 * @param encryptedPassword 加密后的密码
 * @returns 解密后的密码
 */
export const decryptPassword = (encryptedPassword: string): string => {
  try {
    // 解密，使用相同的配置
    const key = CryptoJS.enc.Utf8.parse(SECRET_KEY);
    const decrypted = CryptoJS.AES.decrypt(encryptedPassword, key, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    });
    return decrypted.toString(CryptoJS.enc.Utf8);
  } catch (error) {
    console.error('密码解密失败:', error);
    return '';
  }
}; 