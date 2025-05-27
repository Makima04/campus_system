package com.example.campussysteam.module.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生选课数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseDTO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程编码
     */
    private String courseCode;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 教师名称
     */
    private String teacherName;
    
    /**
     * 开课院系
     */
    private String departmentName;
    
    /**
     * 学分
     */
    private Double credits;
    
    /**
     * 成绩
     */
    private Double score;
    
    /**
     * 出勤率（百分比）
     */
    private Double attendance;
    
    /**
     * 学期
     */
    private String semester;
    
    /**
     * 选课状态：SELECTED(已选), DROPPED(已退), COMPLETED(已完成)
     */
    private String status;
    
    /**
     * 选课时间
     */
    private LocalDateTime selectionTime;

    /**
     * 上课时间
     */
    private String classTime;

    /**
     * 上课地点
     */
    private String classroom;

    /**
     * 上课周次
     */
    private String weeks;

    /**
     * 上课节次
     */
    private String sections;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 