package com.example.campussysteam.config;

import com.example.campussysteam.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.authorization.AuthorizationManager;
import java.util.function.Supplier;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Spring Security配置类
 * 用于配置安全相关的功能，如认证、授权、跨域等
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final ServerConfig serverConfig;

    /**
     * 公开API的安全过滤链，优先级高于主安全配置
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/api/oss/signature", "/api/oss/get-signed-url", "/api/oss/check-config", 
                "/api/oss/list", "/api/oss/download-url", 
                "/api/auth/**", "/api/upload/**", 
                "/file/download/**", "/api/config/public",
                "/api/oss/multipart/**", "/api/redis/**"
            )
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        return http.build();
    }

    /**
     * 自定义管理员权限检查逻辑，同时接受ROLE_ADMIN和ADMIN两种角色
     */
    private AuthorizationManager<RequestAuthorizationContext> hasAdminRole() {
        return (authentication, context) -> {
            Supplier<Authentication> authSupplier = authentication;
            Authentication auth = authSupplier.get();
            
            if (auth == null) {
                return new AuthorizationDecision(false);
            }
            
            boolean hasAdminRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ADMIN"));
                
            return new AuthorizationDecision(hasAdminRole);
        };
    }

    /**
     * 主安全过滤链
     * 定义了系统的安全规则
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护，因为使用了JWT
            .csrf(csrf -> csrf.disable())
            // 启用CORS支持
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许所有人访问/api/auth/**路径
                .requestMatchers("/api/auth/**").permitAll()
                // 允许无需认证访问OSS特定端点，但删除操作需要认证
                .requestMatchers("/api/oss/signature", "/api/oss/get-signed-url", 
                                 "/api/oss/check-config", "/api/oss/list", 
                                 "/api/oss/download-url").permitAll()
                // 添加这一行，允许无需认证访问分片上传API
                .requestMatchers("/api/oss/multipart/**").permitAll()
                // 添加这一行，允许无需认证访问上传API
                .requestMatchers("/api/upload/**").permitAll()
                // 添加这一行，允许无需认证访问文件下载API
                .requestMatchers("/file/download/**").permitAll()
                // 添加这一行，允许无需认证访问公开配置
                .requestMatchers("/api/config/public").permitAll()
                // 添加Redis API无需认证访问
                .requestMatchers("/api/redis/**").permitAll()
                // 添加管理员API的权限配置，同时接受ROLE_ADMIN和ADMIN两种角色
                .requestMatchers("/api/admin/**").access(hasAdminRole())
                // 添加系统日志API的权限配置，同时接受ROLE_ADMIN和ADMIN两种角色
                .requestMatchers("/api/system/logs/**").access(hasAdminRole())
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            // 配置会话管理，使用无状态会话
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 添加认证提供者
            .authenticationProvider(authenticationProvider)
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置CORS（跨域资源共享）
     * 允许前端应用从不同域访问API
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 从配置中获取允许的源列表
        List<String> allowedOrigins = serverConfig.getCorsAllowedOrigins();
        
        // 设置允许的源
        configuration.setAllowedOrigins(allowedOrigins);
        
        // 允许所有常用HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许所有常用请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-User-Role",
            "ROLE",
            "role"
        ));
        
        // 允许发送认证信息(cookies等)
        configuration.setAllowCredentials(true);
        
        // 缓存预检请求结果时间(秒)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 