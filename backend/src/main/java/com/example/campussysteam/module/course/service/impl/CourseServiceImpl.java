package com.example.campussysteam.module.course.service.impl;

import com.example.campussysteam.module.course.dto.CourseDTO;
import com.example.campussysteam.module.course.entity.Course;
import com.example.campussysteam.module.course.repository.CourseRepository;
import com.example.campussysteam.module.course.service.CourseService;
import com.example.campussysteam.module.student.entity.Department;
import com.example.campussysteam.module.student.repository.DepartmentRepository;
import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.module.user.repository.UserRepository;
import com.example.campussysteam.module.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service("moduleCourseService")
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    @Qualifier("moduleCourseRepository")
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RedisService redisService;

    private static final String COURSE_CACHE_PREFIX = "course:";
    private static final String COURSE_LOCK_PREFIX = "lock:course:";
    private static final String HOT_COURSES_KEY = "hot:courses";

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> findAll(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    @Cacheable(value = "course", key = "#id")
    public CourseDTO findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        return convertToDTO(course);
    }

    @Override
    @Transactional
    public CourseDTO create(CourseDTO courseDTO) {
        Course course = convertToEntity(courseDTO);
        course.setSelectedCount(0);
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "course", key = "#id")
    public CourseDTO update(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        
        updateCourseFromDTO(course, courseDTO);
        Course updatedCourse = courseRepository.save(course);
        
        // 清除相关缓存
        redisService.deleteKey(HOT_COURSES_KEY);
        
        return convertToDTO(updatedCourse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "course", key = "#id")
    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "course", key = "#id")
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
    }

    @Override
    @Transactional
    @CacheEvict(value = "course", key = "#course.id")
    public Course updateCourse(Course course) {
        // 更新课程信息
        Course updatedCourse = courseRepository.save(course);
        
        // 清除相关缓存
        redisService.delete(HOT_COURSES_KEY);
        
        return updatedCourse;
    }

    @Override
    public List<Course> getHotCourses() {
        // 使用缓存击穿保护获取热门课程
        return redisService.getWithCachePenetrationProtection(
                HOT_COURSES_KEY,
                "lock:" + HOT_COURSES_KEY,
                courseRepository::findTop10ByOrderBySelectedCountDesc,
                3600 // 1小时过期
        );
    }

    @Override
    public void incrementEnrollmentCount(Long courseId) {
        String key = COURSE_CACHE_PREFIX + courseId + ":enrollment";
        redisService.increment(key, 1L);
    }

    @Override
    public void decrementEnrollmentCount(Long courseId) {
        String key = COURSE_CACHE_PREFIX + courseId + ":enrollment";
        redisService.decrement(key, 1L);
    }

    @Override
    public boolean trySelectCourse(Long courseId, Long studentId) {
        String lockKey = COURSE_LOCK_PREFIX + courseId + ":" + studentId;
        String lockValue = String.valueOf(System.currentTimeMillis());
        
        // 尝试获取分布式锁
        boolean locked = redisService.tryLock(lockKey, lockValue, 10L, TimeUnit.SECONDS);
        
        try {
            if (locked) {
                // 检查课程余量
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("课程不存在"));
                
                if (course.getCapacity() > course.getSelectedCount()) {
                    // 增加已选人数
                    course.setSelectedCount(course.getSelectedCount() + 1);
                    courseRepository.save(course);
                    return true;
                }
                return false;
            }
            return false;
        } finally {
            if (locked) {
                redisService.unlock(lockKey);
            }
        }
    }

    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        
        // 添加对 department 的空值检查
        if (course.getDepartment() != null) {
            dto.setDepartmentId(course.getDepartment().getId());
            dto.setDepartmentName(course.getDepartment().getName());
        } else {
            dto.setDepartmentId(null);
            dto.setDepartmentName("未分配院系");
        }
        
        // 添加对 teacher 的空值检查
        if (course.getTeacher() != null) {
            dto.setTeacherId(course.getTeacher().getId());
            dto.setTeacherName(course.getTeacher().getRealName());
        } else {
            dto.setTeacherId(null);
            dto.setTeacherName("未分配教师");
        }
        
        dto.setCredits(course.getCredits());
        dto.setCourseType(course.getCourseType());
        dto.setSemester(course.getSemester());
        dto.setCapacity(course.getCapacity());
        dto.setSelectedCount(course.getSelectedCount());
        dto.setStatus(course.getStatus());
        dto.setDescription(course.getDescription());
        
        // 添加时间和地点相关字段
        dto.setClassTime(course.getClassTime());
        dto.setClassroom(course.getClassroom());
        dto.setWeeks(course.getWeeks());
        dto.setSections(course.getSections());
        
        return dto;
    }

    private Course convertToEntity(CourseDTO dto) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return Course.builder()
                .courseCode(dto.getCourseCode())
                .courseName(dto.getCourseName())
                .department(department)
                .teacher(teacher)
                .credits(dto.getCredits())
                .courseType(dto.getCourseType())
                .semester(dto.getSemester())
                .capacity(dto.getCapacity())
                .selectedCount(dto.getSelectedCount())
                .status(dto.getStatus())
                .description(dto.getDescription())
                .classTime(dto.getClassTime())
                .classroom(dto.getClassroom())
                .weeks(dto.getWeeks())
                .sections(dto.getSections())
                .build();
    }

    private void updateCourseFromDTO(Course course, CourseDTO dto) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        course.setDepartment(department);
        course.setTeacher(teacher);
        course.setCredits(dto.getCredits());
        course.setCourseType(dto.getCourseType());
        course.setSemester(dto.getSemester());
        course.setCapacity(dto.getCapacity());
        course.setStatus(dto.getStatus());
        course.setDescription(dto.getDescription());
        course.setClassTime(dto.getClassTime());
        course.setClassroom(dto.getClassroom());
        course.setWeeks(dto.getWeeks());
        course.setSections(dto.getSections());
    }
} 