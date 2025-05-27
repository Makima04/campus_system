package com.example.campussysteam.module.student.repository;

import com.example.campussysteam.module.student.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    List<Class> findByMajorId(Long majorId);
    Class findByCode(String code);
    List<Class> findByGrade(Integer grade);
} 