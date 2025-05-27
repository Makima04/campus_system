package com.example.campussysteam.module.course.service.impl;

import com.example.campussysteam.module.course.dto.StudentCourseDTO;
import com.example.campussysteam.module.course.entity.Course;
import com.example.campussysteam.module.course.entity.StudentCourse;
import com.example.campussysteam.module.course.repository.CourseRepository;
import com.example.campussysteam.repository.StudentCourseRepository;
import com.example.campussysteam.module.course.service.StudentCourseService;
import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentCourseServiceImpl implements StudentCourseService {

    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public Page<StudentCourseDTO> getAvailableCourses(Long studentId, String semester, Pageable pageable) {
        // 获取所有课程，并过滤掉学生已选的课程
        return courseRepository.findBySemester(semester, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public List<StudentCourseDTO> getSelectedCourses(Long studentId, String semester) {
        return studentCourseRepository.findByStudentIdAndSemester(studentId, semester)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentCourseDTO enrollCourse(Long studentId, Long courseId) {
        // 检查课程是否存在
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 检查学生是否存在
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));

        // 检查是否已经选过这门课
        if (studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("已经选过这门课程");
        }

        // 检查课程容量
        if (course.getSelectedCount() >= course.getCapacity()) {
            throw new RuntimeException("课程已满");
        }

        // 创建选课记录
        StudentCourse studentCourse = StudentCourse.builder()
                .student(student)
                .course(course)
                .selectionTime(LocalDateTime.now())
                .status("SELECTED")
                .build();

        // 更新课程已选人数
        course.setSelectedCount(course.getSelectedCount() + 1);
        courseRepository.save(course);

        // 保存选课记录
        StudentCourse savedStudentCourse = studentCourseRepository.save(studentCourse);
        return convertToDTO(savedStudentCourse);
    }

    @Override
    @Transactional
    public void dropCourse(Long studentId, Long courseId) {
        // 查找选课记录
        StudentCourse studentCourse = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("未选择该课程"));

        // 更新课程已选人数
        Course course = studentCourse.getCourse();
        course.setSelectedCount(course.getSelectedCount() - 1);
        courseRepository.save(course);

        // 删除选课记录
        studentCourseRepository.delete(studentCourse);
    }

    @Override
    public List<StudentCourseDTO> getStudentSchedule(Long studentId, String semester) {
        return studentCourseRepository.findByStudentIdAndSemester(studentId, semester)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkTimeConflict(Long studentId, Long courseId) {
        // 获取要选的课程
        Course targetCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 获取学生已选的所有课程
        List<StudentCourse> selectedCourses = studentCourseRepository.findByStudentId(studentId);

        // 检查时间冲突
        for (StudentCourse selectedCourse : selectedCourses) {
            Course course = selectedCourse.getCourse();
            if (hasTimeConflict(course, targetCourse)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public StudentCourseDTO getStudentCourseDetail(Long studentId, Long courseId) {
        return studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("未找到课程记录"));
    }

    private StudentCourseDTO convertToDTO(StudentCourse studentCourse) {
        StudentCourseDTO dto = new StudentCourseDTO();
        dto.setId(studentCourse.getId());
        dto.setStudentId(studentCourse.getStudent().getId());
        dto.setStudentName(studentCourse.getStudent().getRealName());
        dto.setCourseId(studentCourse.getCourse().getId());
        dto.setCourseName(studentCourse.getCourse().getCourseName());
        dto.setCourseCode(studentCourse.getCourse().getCourseCode());
        dto.setTeacherName(studentCourse.getCourse().getTeacher().getRealName());
        dto.setClassTime(studentCourse.getCourse().getClassTime());
        dto.setClassroom(studentCourse.getCourse().getClassroom());
        dto.setWeeks(studentCourse.getCourse().getWeeks());
        dto.setSections(studentCourse.getCourse().getSections());
        dto.setSelectionTime(studentCourse.getSelectionTime());
        dto.setScore(studentCourse.getScore());
        dto.setAttendance(studentCourse.getAttendance());
        dto.setStatus(studentCourse.getStatus());
        return dto;
    }

    private StudentCourseDTO convertToDTO(Course course) {
        StudentCourseDTO dto = new StudentCourseDTO();
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCourseCode(course.getCourseCode());
        dto.setTeacherName(course.getTeacher().getRealName());
        dto.setClassTime(course.getClassTime());
        dto.setClassroom(course.getClassroom());
        dto.setWeeks(course.getWeeks());
        dto.setSections(course.getSections());
        return dto;
    }

    private boolean hasTimeConflict(Course course1, Course course2) {
        // 如果两门课程的周次或节次有重叠，则存在时间冲突
        return hasOverlap(course1.getWeeks(), course2.getWeeks()) &&
               hasOverlap(course1.getSections(), course2.getSections());
    }

    private boolean hasOverlap(String range1, String range2) {
        // 解析范围字符串，例如："1-8,10-16"
        String[] ranges1 = range1.split(",");
        String[] ranges2 = range2.split(",");

        for (String r1 : ranges1) {
            String[] bounds1 = r1.split("-");
            int start1 = Integer.parseInt(bounds1[0]);
            int end1 = Integer.parseInt(bounds1[1]);

            for (String r2 : ranges2) {
                String[] bounds2 = r2.split("-");
                int start2 = Integer.parseInt(bounds2[0]);
                int end2 = Integer.parseInt(bounds2[1]);

                // 检查两个范围是否重叠
                if (!(end1 < start2 || start1 > end2)) {
                    return true;
                }
            }
        }

        return false;
    }
} 