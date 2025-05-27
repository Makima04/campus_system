package com.example.campussysteam.module.student.repository;

import com.example.campussysteam.module.student.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    List<Major> findByDepartmentId(Long departmentId);
    Major findByCode(String code);
} 