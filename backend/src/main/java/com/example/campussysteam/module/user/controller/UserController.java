package com.example.campussysteam.module.user.controller;

import com.example.campussysteam.common.log.Log;
import com.example.campussysteam.common.log.OperationType;
import com.example.campussysteam.common.service.PasswordService;
import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.module.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PasswordService passwordService;

    public UserController(UserService userService, PasswordService passwordService) {
        this.userService = userService;
        this.passwordService = passwordService;
    }

    @PutMapping("/{id}")
    @Log(module = "用户管理", type = OperationType.UPDATE, description = "更新用户信息")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            // 解密密码
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordService.handlePasswordDecryption(user.getPassword()));
            }
            
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
} 