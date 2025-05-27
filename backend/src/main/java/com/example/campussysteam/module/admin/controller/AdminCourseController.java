package com.example.campussysteam.module.admin.controller;

import com.example.campussysteam.common.ResultDTO;
import com.example.campussysteam.common.ResultCode;
import com.example.campussysteam.module.course.dto.CourseDTO;
import com.example.campussysteam.module.course.service.CourseService;
import com.example.campussysteam.module.student.entity.Department;
import com.example.campussysteam.module.student.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员课程管理控制器
 * 处理/api/admin/course相关请求，接收前端admin模块的API请求
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;
    private final DepartmentRepository departmentRepository;

    /**
     * 获取课程列表
     */
    @GetMapping("/course/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CourseDTO>> listCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(courseService.findAll(pageRequest));
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/course/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    /**
     * 创建课程
     */
    @PostMapping("/course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.create(courseDTO));
    }

    /**
     * 更新课程
     */
    @PutMapping("/course/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.update(id, courseDTO));
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/course/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取院系列表
     */
    @GetMapping("/department/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResultDTO<List<Department>>> listDepartments() {
        List<Department> departments = departmentRepository.findAll();
        ResultDTO<List<Department>> result = new ResultDTO<>(
            ResultCode.SUCCESS.getCode(), 
            "获取成功", 
            departments
        );
        return ResponseEntity.ok(result);
    }
} 