package com.example.campussysteam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * 服务器配置类
 * 集中管理服务器相关的IP地址和端口信息
 */
@Configuration
public class ServerConfig {

    @Value("${ip-config.env}")
    private String env;
    
    // 应用服务器主机
    @Value("${ip-config.server.host}")
    private String serverHost;

    // 应用服务器端口
    @Value("${ip-config.server.port}")
    private int configServerPort;

    // 数据库主机
    @Value("${ip-config.database.host}")
    private String databaseHost;

    // 数据库端口
    @Value("${ip-config.database.port}")
    private int databasePort;

    // Redis主机
    @Value("${ip-config.redis.host}")
    private String redisHost;

    // Redis端口
    @Value("${ip-config.redis.port}")
    private int redisPort;

    // OSS端点
    @Value("${ip-config.aliyun.oss-endpoint}")
    private String ossEndpoint;
    
    // 前端URL
    @Value("${ip-config.frontend.url}")
    private String frontendUrl;
    
    // 不再从配置文件中读取，直接硬编码
    private List<String> developmentCorsOrigins = Arrays.asList(
        "http://localhost:80",
        "http://localhost:5173",
        "http://localhost:8080"
    );
    
    // 不再从配置文件中读取，直接硬编码
    private List<String> productionCorsOrigins = Arrays.asList(
        "http://47.109.158.246",
        "http://47.109.158.246:5173"
    );

    public String getEnv() {
        return env;
    }
    
    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return configServerPort;
    }

    public String getDatabaseHost() {
        return databaseHost;
    }

    public int getDatabasePort() {
        return databasePort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getOssEndpoint() {
        return ossEndpoint;
    }
    
    public String getFrontendUrl() {
        return frontendUrl;
    }
    
    public List<String> getCorsAllowedOrigins() {
        return "development".equals(env) ? developmentCorsOrigins : productionCorsOrigins;
    }
    
    public List<String> getEnvironmentCorsOrigins() {
        return "development".equals(env) ? developmentCorsOrigins : productionCorsOrigins;
    }

    // 获取服务器的完整URL
    public String getServerUrl() {
        return "http://" + serverHost + ":" + getServerPort();
    }

    // 获取数据库连接URL（不包含参数）
    public String getDatabaseUrl() {
        return "jdbc:mysql://" + databaseHost + ":" + databasePort + "/campus_system";
    }

    // 获取Redis连接URL
    public String getRedisUrl() {
        return redisHost + ":" + redisPort;
    }
} 