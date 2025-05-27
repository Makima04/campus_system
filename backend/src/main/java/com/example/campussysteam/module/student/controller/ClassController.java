package com.example.campussysteam.module.student.controller;

import com.example.campussysteam.common.log.Log;
import com.example.campussysteam.common.log.OperationType;
import com.example.campussysteam.module.student.entity.Class;
import com.example.campussysteam.module.student.repository.ClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassRepository classRepository;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Log(module = "班级管理", type = OperationType.QUERY, description = "查询班级列表")
    public ResponseEntity<List<Class>> list(@RequestParam(required = false) Long majorId) {
        if (majorId != null) {
            return ResponseEntity.ok(classRepository.findByMajorId(majorId));
        }
        return ResponseEntity.ok(classRepository.findAll());
    }
} 