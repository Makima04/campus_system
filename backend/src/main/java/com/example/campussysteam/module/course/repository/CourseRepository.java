package com.example.campussysteam.module.course.repository;

import com.example.campussysteam.module.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("moduleCourseRepository")
public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = {"department", "teacher"})
    @Query("SELECT c FROM Course c")
    Page<Course> findAllWithDetails(Pageable pageable);

    boolean existsByCourseCode(String courseCode);

    // 根据课程代码查找课程
    Course findByCourseCode(String courseCode);
    
    // 根据课程名称查找课程
    List<Course> findByCourseNameContaining(String courseName);
    
    // 根据院系ID查找课程
    List<Course> findByDepartmentId(Long departmentId);
    
    // 根据教师ID查找课程
    List<Course> findByTeacherId(Long teacherId);
    
    // 查找选课人数最多的前10门课程
    List<Course> findTop10ByOrderBySelectedCountDesc();

    // 根据学期查找课程
    Page<Course> findBySemester(String semester, Pageable pageable);
} 