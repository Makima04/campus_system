package com.example.campussysteam.module.auth.service.impl;

import com.example.campussysteam.module.auth.dto.AuthRequest;
import com.example.campussysteam.module.auth.dto.AuthResponse;
import com.example.campussysteam.module.auth.dto.RegisterRequest;
import com.example.campussysteam.module.user.entity.Role;
import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.module.user.repository.UserRepository;
import com.example.campussysteam.security.JwtService;
import com.example.campussysteam.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 * 处理用户注册和登录的具体逻辑
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * 用户注册
     * 1. 创建新用户
     * 2. 生成JWT令牌
     * 3. 返回认证响应
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 创建新用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        // 保存用户
        User savedUser = userRepository.save(user);

        // 生成JWT令牌
        String token = jwtService.generateToken(user);
        
        // 返回认证响应
        return AuthResponse.builder()
                .token(token)
                .user(savedUser)
                .build();
    }

    /**
     * 用户登录
     * 1. 验证用户名和密码
     * 2. 获取用户信息
     * 3. 生成JWT令牌
     * 4. 返回认证响应
     */
    @Override
    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            // 如果认证成功，生成JWT令牌
            var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
            
            var jwtToken = jwtService.generateToken(user);
            
            return AuthResponse.builder()
                .token(jwtToken)
                .build();
        } catch (Exception e) {
            logger.error("认证失败: {}", e.getMessage());
            throw new RuntimeException("认证失败", e);
        }
    }
} 