package com.example.campussysteam.common.service;

import com.example.campussysteam.common.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);
    
    private final CryptoUtil cryptoUtil;
    
    /**
     * 处理密码解密
     * @param encryptedPassword 加密后的密码
     * @return 解密后的密码
     */
    public String handlePasswordDecryption(String encryptedPassword) {
        if (encryptedPassword != null && !encryptedPassword.isEmpty()) {
            try {
                String decryptedPassword = cryptoUtil.decrypt(encryptedPassword);
                logger.debug("密码解密成功");
                return decryptedPassword;
            } catch (Exception e) {
                logger.warn("密码解密失败，将使用原始密码：{}", e.getMessage());
                // 如果解密失败，使用原始密码继续
                return encryptedPassword;
            }
        }
        return encryptedPassword;
    }
} 