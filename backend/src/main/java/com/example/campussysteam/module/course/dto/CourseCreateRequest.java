package com.example.campussysteam.module.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 课程创建请求
 */
@Data
public class CourseCreateRequest {
    
    /**
     * 课程编号
     */
    @NotBlank(message = "课程编号不能为空")
    @Size(min = 6, max = 20, message = "课程编号长度应在6-20之间")
    private String courseCode;
    
    /**
     * 课程名称
     */
    @NotBlank(message = "课程名称不能为空")
    @Size(min = 2, max = 50, message = "课程名称长度应在2-50之间")
    private String courseName;
    
    /**
     * 开课院系ID
     */
    @NotNull(message = "开课院系不能为空")
    @Positive(message = "开课院系ID必须为正数")
    private Long departmentId;
    
    /**
     * 教师ID
     */
    @NotNull(message = "授课教师不能为空")
    @Positive(message = "教师ID必须为正数")
    private Long teacherId;
    
    /**
     * 学分
     */
    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.5", message = "学分最小为0.5")
    @DecimalMax(value = "10.0", message = "学分最大为10")
    private Double credits;
    
    /**
     * 课程类型
     */
    @NotBlank(message = "课程类型不能为空")
    @Pattern(regexp = "REQUIRED|ELECTIVE|PUBLIC", message = "课程类型必须为REQUIRED, ELECTIVE或PUBLIC")
    private String courseType;
    
    /**
     * 学期
     */
    @NotBlank(message = "学期不能为空")
    @Pattern(regexp = "\\d{4}-\\d{4}-[1-2]", message = "学期格式必须为xxxx-xxxx-1或xxxx-xxxx-2")
    private String semester;
    
    /**
     * 课程容量
     */
    @NotNull(message = "课程容量不能为空")
    @Min(value = 1, message = "课程容量最小为1")
    @Max(value = 500, message = "课程容量最大为500")
    private Integer capacity;
    
    /**
     * 课程状态
     */
    @NotBlank(message = "课程状态不能为空")
    @Pattern(regexp = "NOT_STARTED|IN_PROGRESS|ENDED", message = "课程状态必须为NOT_STARTED, IN_PROGRESS或ENDED")
    private String status;
    
    /**
     * 课程描述
     */
    @Size(max = 500, message = "课程描述长度不能超过500")
    private String description;

    /**
     * 上课时间
     */
    @NotBlank(message = "上课时间不能为空")
    private String classTime;

    /**
     * 上课地点
     */
    @NotBlank(message = "上课地点不能为空")
    private String classroom;

    /**
     * 上课周次
     */
    @NotBlank(message = "上课周次不能为空")
    @Pattern(regexp = "^[1-9][0-9]?(-[1-9][0-9]?)?(,[1-9][0-9]?(-[1-9][0-9]?)?)*$", 
            message = "周次格式不正确，例如：1-8,10-16")
    private String weeks;

    /**
     * 上课节次
     */
    @NotBlank(message = "上课节次不能为空")
    @Pattern(regexp = "^[1-9][0-9]?(-[1-9][0-9]?)?(,[1-9][0-9]?(-[1-9][0-9]?)?)*$", 
            message = "节次格式不正确，例如：1-2,3-4")
    private String sections;
} 