package com.example.campussysteam.module.course.controller;

import com.example.campussysteam.common.log.Log;
import com.example.campussysteam.common.log.OperationType;
import com.example.campussysteam.module.course.dto.StudentCourseDTO;
import com.example.campussysteam.module.course.service.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生课程控制器
 */
@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {
    
    private final StudentCourseService studentCourseService;
    
    /**
     * 获取可选课程列表
     */
    @GetMapping("/available")
    @Log(module = "选课管理", type = OperationType.QUERY, description = "查询可选课程")
    public ResponseEntity<Page<StudentCourseDTO>> getAvailableCourses(
            @RequestParam Long studentId,
            @RequestParam String semester,
            Pageable pageable) {
        return ResponseEntity.ok(studentCourseService.getAvailableCourses(studentId, semester, pageable));
    }
    
    /**
     * 获取已选课程列表
     */
    @GetMapping("/selected")
    @Log(module = "选课管理", type = OperationType.QUERY, description = "查询已选课程")
    public ResponseEntity<List<StudentCourseDTO>> getSelectedCourses(
            @RequestParam Long studentId,
            @RequestParam String semester) {
        return ResponseEntity.ok(studentCourseService.getSelectedCourses(studentId, semester));
    }
    
    /**
     * 选课
     */
    @PostMapping("/{courseId}/enroll")
    @Log(module = "选课管理", type = OperationType.INSERT, description = "学生选课")
    public ResponseEntity<StudentCourseDTO> enrollCourse(
            @RequestParam Long studentId,
            @PathVariable Long courseId) {
        // 先检查时间冲突
        if (studentCourseService.checkTimeConflict(studentId, courseId)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(studentCourseService.enrollCourse(studentId, courseId));
    }
    
    /**
     * 退课
     */
    @DeleteMapping("/{courseId}/drop")
    @Log(module = "选课管理", type = OperationType.DELETE, description = "学生退课")
    public ResponseEntity<Void> dropCourse(
            @RequestParam Long studentId,
            @PathVariable Long courseId) {
        studentCourseService.dropCourse(studentId, courseId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取课程表
     */
    @GetMapping("/schedule")
    @Log(module = "选课管理", type = OperationType.QUERY, description = "查询课程表")
    public ResponseEntity<List<StudentCourseDTO>> getSchedule(
            @RequestParam Long studentId,
            @RequestParam String semester) {
        return ResponseEntity.ok(studentCourseService.getStudentSchedule(studentId, semester));
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{courseId}")
    @Log(module = "选课管理", type = OperationType.QUERY, description = "查询课程详情")
    public ResponseEntity<StudentCourseDTO> getCourseDetail(
            @RequestParam Long studentId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(studentCourseService.getStudentCourseDetail(studentId, courseId));
    }
} 