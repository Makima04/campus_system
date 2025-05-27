package com.example.campussysteam.module.config.controller;

import com.example.campussysteam.module.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 配置控制器
 * 提供系统配置相关的API
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;
    
    /**
     * 获取公开的配置信息
     * 不需要认证即可访问
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicConfig() {
        return ResponseEntity.ok(configService.getPublicConfigInfo());
    }
    
    /**
     * 获取完整的服务器配置信息
     * 仅管理员可以访问
     */
    @GetMapping("/server")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getServerConfig() {
        return ResponseEntity.ok(configService.getServerConfigInfo());
    }
} 