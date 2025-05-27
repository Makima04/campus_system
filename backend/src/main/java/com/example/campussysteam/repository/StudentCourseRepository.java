package com.example.campussysteam.repository;

import com.example.campussysteam.module.course.entity.StudentCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 学生课程关联数据访问接口
 */
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    
    /**
     * 根据学生ID和课程ID查询选课记录
     */
    Optional<StudentCourse> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    /**
     * 查询学生的所有选课记录
     */
    List<StudentCourse> findByStudentId(Long studentId);
    
    /**
     * 分页查询学生的选课记录
     */
    Page<StudentCourse> findByStudentId(Long studentId, Pageable pageable);
    
    /**
     * 查询课程的所有学生
     */
    List<StudentCourse> findByCourseId(Long courseId);
    
    /**
     * 分页查询课程的学生
     */
    Page<StudentCourse> findByCourseId(Long courseId, Pageable pageable);
    
    /**
     * 查询学生在特定学期的选课记录
     */
    @Query("SELECT sc FROM StudentCourse sc JOIN sc.course c WHERE sc.student.id = :studentId AND c.semester = :semester")
    List<StudentCourse> findByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("semester") String semester);
    
    /**
     * 查询学生的成绩统计
     */
    @Query("SELECT AVG(sc.score) FROM StudentCourse sc WHERE sc.student.id = :studentId AND sc.score IS NOT NULL")
    Double getStudentAverageScore(@Param("studentId") Long studentId);
    
    /**
     * 查询课程的平均成绩
     */
    @Query("SELECT AVG(sc.score) FROM StudentCourse sc WHERE sc.course.id = :courseId AND sc.score IS NOT NULL")
    Double getCourseAverageScore(@Param("courseId") Long courseId);
    
    /**
     * 检查学生是否已选修某课程
     */
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    /**
     * 统计课程的已选学生数
     */
    long countByCourseId(Long courseId);
} 