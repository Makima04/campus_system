package com.example.campussysteam.module.config.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigService {

    public Map<String, Object> getPublicConfigInfo() {
        Map<String, Object> config = new HashMap<>();
        // 添加公开配置信息
        config.put("appName", "校园管理系统");
        config.put("version", "1.0.0");
        return config;
    }

    public Map<String, Object> getServerConfigInfo() {
        Map<String, Object> config = new HashMap<>();
        // 添加服务器配置信息
        config.put("serverName", "校园管理系统服务器");
        config.put("environment", "production");
        config.put("maxUploadSize", "10MB");
        return config;
    }
} 