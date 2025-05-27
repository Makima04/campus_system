package com.example.campussysteam.module.course.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学院更新请求
 */
@Data
public class DepartmentUpdateRequest {
    
    /**
     * 学院ID
     */
    @NotNull(message = "学院ID不能为空")
    @Positive(message = "学院ID必须为正数")
    private Long id;
    
    /**
     * 学院编码
     */
    @Size(min = 2, max = 20, message = "学院编码长度应在2-20之间")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "学院编码只能包含大写字母和数字")
    private String code;
    
    /**
     * 学院名称
     */
    @Size(min = 2, max = 50, message = "学院名称长度应在2-50之间")
    private String name;
    
    /**
     * 学院描述
     */
    @Size(max = 500, message = "学院描述长度不能超过500")
    private String description;
} 