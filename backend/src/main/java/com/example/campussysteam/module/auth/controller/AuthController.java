package com.example.campussysteam.module.auth.controller;

import com.example.campussysteam.common.api.Result;
import com.example.campussysteam.common.log.Log;
import com.example.campussysteam.common.log.OperationType;
import com.example.campussysteam.common.service.PasswordService;
import com.example.campussysteam.module.auth.dto.AuthRequest;
import com.example.campussysteam.module.auth.dto.AuthResponse;
import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.module.auth.service.AuthService;
import com.example.campussysteam.module.user.service.UserService;
import com.example.campussysteam.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Log(module = "认证管理", type = OperationType.LOGIN, description = "用户登录")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            // 解密密码
            String decryptedPassword = passwordService.handlePasswordDecryption(password);
            
            // 认证用户
            String token = userService.authenticate(username, decryptedPassword);
            
            // 获取用户信息
            User user = userService.getUserByUsername(username);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole(),
                "email", user.getEmail(),
                "phone", user.getPhone()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("登录失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Log(module = "认证管理", type = OperationType.INSERT, description = "用户注册")
    public ResponseEntity<Result<User>> register(@RequestBody User user) {
        try {
            // 解密密码
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordService.handlePasswordDecryption(user.getPassword()));
            }
            
            User registeredUser = userService.createUser(user);
            return ResponseEntity.ok(Result.success(registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Log(module = "认证管理", type = OperationType.LOGOUT, description = "用户登出")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // 移除"Bearer "前缀
            jwtService.invalidateToken(token);
            return ResponseEntity.ok("登出成功");
        } catch (Exception e) {
            logger.error("登出失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("登出失败");
        }
    }
} 