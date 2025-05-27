package com.example.campussysteam.module.course.service;

import com.example.campussysteam.module.course.dto.CourseDTO;
import com.example.campussysteam.module.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CourseService {
    Page<CourseDTO> findAll(Pageable pageable);
    CourseDTO findById(Long id);
    CourseDTO create(CourseDTO courseDTO);
    CourseDTO update(Long id, CourseDTO courseDTO);
    void delete(Long id);
    
    // 获取课程详情（带缓存）
    Course getCourseById(Long id);
    
    // 更新课程信息（带缓存）
    Course updateCourse(Course course);
    
    // 获取热门课程
    List<Course> getHotCourses();
    
    // 增加选课人数
    void incrementEnrollmentCount(Long courseId);
    
    // 减少选课人数
    void decrementEnrollmentCount(Long courseId);
    
    // 尝试选课
    boolean trySelectCourse(Long courseId, Long studentId);
} 