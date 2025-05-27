package com.example.campussysteam.module.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    
    /**
     * 课程ID
     */
    private Long id;
    
    /**
     * 课程编号
     */
    private String courseCode;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 开课院系ID
     */
    private Long departmentId;
    
    /**
     * 院系名称
     */
    private String departmentName;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 学分
     */
    private Double credits;
    
    /**
     * 课程类型
     */
    private String courseType;
    
    /**
     * 学期
     */
    private String semester;
    
    /**
     * 课程容量
     */
    private Integer capacity;
    
    /**
     * 已选人数
     */
    private Integer selectedCount;
    
    /**
     * 课程状态
     */
    private String status;
    
    /**
     * 课程描述
     */
    private String description;

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
} 