package com.example.campussysteam.module.student.repository;

import com.example.campussysteam.module.student.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByCode(String code);
    Department findByName(String name);
} 