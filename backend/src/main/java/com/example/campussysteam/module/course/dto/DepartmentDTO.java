package com.example.campussysteam.module.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 院系数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    
    /**
     * 院系ID
     */
    private Long id;
    
    /**
     * 院系编码
     */
    private String code;
    
    /**
     * 院系名称
     */
    private String name;
    
    /**
     * 院系简介
     */
    private String description;
} 