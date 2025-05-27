package com.example.campussysteam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 日志配置类
 */
@Configuration
@EnableAspectJAutoProxy
@EnableJpaAuditing
public class LogConfig {

    /**
     * 配置用于日志的ObjectMapper
     * 由于JacksonConfig已经提供了全局的ObjectMapper，这里不再需要
     * 如果特殊需求，可以使用不同的名称
     */
    /*
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
    */
} 