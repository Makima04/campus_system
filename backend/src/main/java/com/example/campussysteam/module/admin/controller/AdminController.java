package com.example.campussysteam.module.admin.controller;

import com.example.campussysteam.common.api.Result;
import com.example.campussysteam.module.user.repository.UserRepository;
import com.example.campussysteam.module.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<Map<String, Object>>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取用户总数
        statistics.put("userCount", userRepository.count());
        
        // 获取学生总数
        statistics.put("studentCount", studentRepository.count());
        
        // 暂时返回0，等后续实现相关功能后再更新
        statistics.put("courseCount", 0);
        statistics.put("selectionCount", 0);
        statistics.put("transactionCount", 0);
        
        return ResponseEntity.ok(Result.success(statistics));
    }
} 