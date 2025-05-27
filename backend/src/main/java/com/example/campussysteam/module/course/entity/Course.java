package com.example.campussysteam.module.course.entity;

import com.example.campussysteam.module.student.entity.Department;
import com.example.campussysteam.module.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 课程编号
     */
    @Column(nullable = false, unique = true, length = 20)
    private String courseCode;
    
    /**
     * 课程名称
     */
    @Column(nullable = false, length = 50)
    private String courseName;
    
    /**
     * 开课院系
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    
    /**
     * 教师ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    /**
     * 教师姓名（冗余字段，便于展示）
     */
    @Column(length = 50)
    private String teacherName;
    
    /**
     * 学分
     */
    @Column(nullable = false)
    private Double credits;
    
    /**
     * 课程类型: REQUIRED(必修), ELECTIVE(选修), PUBLIC(公共课)
     */
    @Column(nullable = false, length = 20)
    private String courseType;
    
    /**
     * 学期，例如: 2023-2024-1
     */
    @Column(nullable = false, length = 20)
    private String semester;
    
    /**
     * 上课时间
     */
    @Column(length = 100)
    private String classTime;
    
    /**
     * 上课地点
     */
    @Column(length = 100)
    private String classroom;
    
    /**
     * 上课周次
     */
    @Column(length = 50)
    private String weeks;
    
    /**
     * 上课节次
     */
    @Column(length = 50)
    private String sections;
    
    /**
     * 课程容量
     */
    @Column(nullable = false)
    private Integer capacity;
    
    /**
     * 已选人数
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer selectedCount = 0;
    
    /**
     * 课程状态: NOT_STARTED(未开始), IN_PROGRESS(进行中), ENDED(已结束)
     */
    @Column(nullable = false, length = 20)
    private String status;
    
    /**
     * 课程描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 