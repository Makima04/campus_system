package com.example.campussysteam.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 环境切换工具类
 * 允许切换开发环境和生产环境的配置
 */
@Component
@Profile("!production") // 只在非生产环境中激活
public class EnvSwitcher {

    private final Path configPath;
    private final Environment environment;

    @Autowired
    public EnvSwitcher(Environment environment) {
        this.environment = environment;
        this.configPath = Paths.get("src/main/resources/application.yml");
    }

    /**
     * 切换运行环境
     * @param env 环境名称（development 或 production）
     * @return 是否切换成功
     */
    @SuppressWarnings("unchecked")
    public boolean switchEnvironment(String env) {
        if (!("development".equals(env) || "production".equals(env))) {
            return false;
        }

        try {
            // 读取YAML文件
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData;
            
            try (FileInputStream in = new FileInputStream(configPath.toFile())) {
                yamlData = yaml.load(in);
            }
            
            // 获取ip-config部分
            Map<String, Object> ipConfig = (Map<String, Object>) yamlData.get("ip-config");
            if (ipConfig == null) {
                ipConfig = new LinkedHashMap<>();
                yamlData.put("ip-config", ipConfig);
            }
            
            // 设置环境
            ipConfig.put("env", env);
            
            // 根据环境更新配置
            if ("development".equals(env)) {
                // 开发环境配置
                updateServerConfig(ipConfig, "localhost", 80);
                updateFrontendConfig(ipConfig, "http://localhost:5173");
            } else if ("production".equals(env)) {
                // 生产环境配置
                updateServerConfig(ipConfig, "47.109.158.246", 80);
                updateFrontendConfig(ipConfig, "http://47.109.158.246");
            }
            
            // 保存YAML文件
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            
            Yaml outputYaml = new Yaml(options);
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                outputYaml.dump(yamlData, writer);
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 更新服务器配置
     */
    @SuppressWarnings("unchecked")
    private void updateServerConfig(Map<String, Object> ipConfig, String host, int port) {
        Map<String, Object> serverConfig = (Map<String, Object>) ipConfig.get("server");
        if (serverConfig == null) {
            serverConfig = new LinkedHashMap<>();
            ipConfig.put("server", serverConfig);
        }
        serverConfig.put("host", host);
        serverConfig.put("port", port);
    }
    
    /**
     * 更新前端配置
     */
    @SuppressWarnings("unchecked")
    private void updateFrontendConfig(Map<String, Object> ipConfig, String url) {
        Map<String, Object> frontendConfig = (Map<String, Object>) ipConfig.get("frontend");
        if (frontendConfig == null) {
            frontendConfig = new LinkedHashMap<>();
            ipConfig.put("frontend", frontendConfig);
        }
        frontendConfig.put("url", url);
    }

    /**
     * 获取当前运行环境
     * @return 当前环境名称
     */
    public String getCurrentEnvironment() {
        return environment.getProperty("ip-config.env");
    }
} 