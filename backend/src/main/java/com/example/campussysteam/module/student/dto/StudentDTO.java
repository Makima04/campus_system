package com.example.campussysteam.module.student.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudentDTO {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String studentId;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private Long majorId;
    private String majorName;
    private Long classId;
    private String className;
    private Integer enrollYear;
    private String status;
} 