package com.example.campussysteam.module.course.service;

import com.example.campussysteam.module.course.dto.StudentCourseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 学生课程服务接口
 */
public interface StudentCourseService {
    
    /**
     * 获取学生可选课程列表
     */
    Page<StudentCourseDTO> getAvailableCourses(Long studentId, String semester, Pageable pageable);
    
    /**
     * 获取学生已选课程列表
     */
    List<StudentCourseDTO> getSelectedCourses(Long studentId, String semester);
    
    /**
     * 选课
     */
    StudentCourseDTO enrollCourse(Long studentId, Long courseId);
    
    /**
     * 退课
     */
    void dropCourse(Long studentId, Long courseId);
    
    /**
     * 获取学生课程表
     */
    List<StudentCourseDTO> getStudentSchedule(Long studentId, String semester);
    
    /**
     * 检查课程冲突
     */
    boolean checkTimeConflict(Long studentId, Long courseId);
    
    /**
     * 获取学生某门课程的详细信息
     */
    StudentCourseDTO getStudentCourseDetail(Long studentId, Long courseId);
} 