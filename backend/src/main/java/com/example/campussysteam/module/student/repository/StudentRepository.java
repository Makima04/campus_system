package com.example.campussysteam.module.student.repository;

import com.example.campussysteam.module.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("SELECT s FROM Student s " +
           "LEFT JOIN s.user u " +
           "LEFT JOIN s.classInfo c " +
           "LEFT JOIN c.major m " +
           "LEFT JOIN m.department d")
    Page<Student> findAllWithDetails(Pageable pageable);
} 