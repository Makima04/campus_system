package com.example.campussysteam.module.student.service;

import com.example.campussysteam.module.student.dto.StudentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {
    Page<StudentDTO> findAll(Pageable pageable);
    StudentDTO findById(Long id);
    StudentDTO create(StudentDTO studentDTO);
    StudentDTO update(Long id, StudentDTO studentDTO);
    void delete(Long id);
} 