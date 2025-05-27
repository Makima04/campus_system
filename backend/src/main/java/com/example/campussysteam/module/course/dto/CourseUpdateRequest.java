package com.example.campussysteam.module.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 课程更新请求
 */
@Data
public class CourseUpdateRequest {
    
    /**
     * 课程编号
     */
    @Size(min = 6, max = 20, message = "课程编号长度应在6-20之间")
    private String courseCode;
    
    /**
     * 课程名称
     */
    @Size(min = 2, max = 50, message = "课程名称长度应在2-50之间")
    private String courseName;
    
    /**
     * 开课院系ID
     */
    @Positive(message = "开课院系ID必须为正数")
    private Long departmentId;
    
    /**
     * 教师ID
     */
    @Positive(message = "教师ID必须为正数")
    private Long teacherId;
    
    /**
     * 学分
     */
    @DecimalMin(value = "0.5", message = "学分最小为0.5")
    @DecimalMax(value = "10.0", message = "学分最大为10")
    private Double credits;
    
    /**
     * 课程类型
     */
    @Pattern(regexp = "REQUIRED|ELECTIVE|PUBLIC", message = "课程类型必须为REQUIRED, ELECTIVE或PUBLIC")
    private String courseType;
    
    /**
     * 学期
     */
    @Pattern(regexp = "\\d{4}-\\d{4}-[1-2]", message = "学期格式必须为xxxx-xxxx-1或xxxx-xxxx-2")
    private String semester;
    
    /**
     * 课程容量
     */
    @Min(value = 1, message = "课程容量最小为1")
    @Max(value = 500, message = "课程容量最大为500")
    private Integer capacity;
    
    /**
     * 课程状态
     */
    @Pattern(regexp = "NOT_STARTED|IN_PROGRESS|ENDED", message = "课程状态必须为NOT_STARTED, IN_PROGRESS或ENDED")
    private String status;
    
    /**
     * 课程描述
     */
    @Size(max = 500, message = "课程描述长度不能超过500")
    private String description;
} 