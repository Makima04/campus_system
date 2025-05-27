package com.example.campussysteam.module.student.controller;

import com.example.campussysteam.common.ResultDTO;
import com.example.campussysteam.common.ResultCode;
import com.example.campussysteam.common.log.Log;
import com.example.campussysteam.common.log.OperationType;
import com.example.campussysteam.module.student.entity.Major;
import com.example.campussysteam.module.student.repository.MajorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/major")
public class MajorController {

    @Autowired
    private MajorRepository majorRepository;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Log(module = "专业管理", type = OperationType.QUERY, description = "查询专业列表")
    public ResultDTO<List<Major>> list(@RequestParam(required = false) Long departmentId) {
        List<Major> majors;
        if (departmentId != null) {
            majors = majorRepository.findByDepartmentId(departmentId);
        } else {
            majors = majorRepository.findAll();
        }
        return new ResultDTO<>(ResultCode.SUCCESS.getCode(), "获取成功", majors);
    }
} 