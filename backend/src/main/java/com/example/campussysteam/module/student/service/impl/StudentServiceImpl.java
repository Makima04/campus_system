package com.example.campussysteam.module.student.service.impl;

import com.example.campussysteam.module.student.dto.StudentDTO;
import com.example.campussysteam.module.student.entity.Class;
import com.example.campussysteam.module.student.entity.Department;
import com.example.campussysteam.module.student.entity.Student;
import com.example.campussysteam.module.student.repository.ClassRepository;
import com.example.campussysteam.module.student.repository.DepartmentRepository;
import com.example.campussysteam.module.student.repository.StudentRepository;
import com.example.campussysteam.module.student.service.StudentService;
import com.example.campussysteam.module.user.entity.Role;
import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<StudentDTO> findAll(Pageable pageable) {
        return studentRepository.findAllWithDetails(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDTO findById(Long id) {
        return studentRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    @Override
    @Transactional
    public StudentDTO create(StudentDTO studentDTO) {
        // 格式化学号
        String formattedStudentId = formatStudentId(studentDTO);
        
        // 创建用户
        User user = new User();
        user.setUsername(studentDTO.getUsername());
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setRealName(studentDTO.getRealName());
        user.setRole(Role.ROLE_STUDENT);
        user.setEmail(studentDTO.getEmail());
        user.setPhone(studentDTO.getPhone());
        user.setStatus(studentDTO.getStatus());
        user = userRepository.save(user);
        
        // 获取班级信息
        Class classInfo = classRepository.findById(studentDTO.getClassId())
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        // 创建学生
        Student student = new Student();
        student.setUser(user);
        student.setStudentId(formattedStudentId);
        student.setClassInfo(classInfo);
        student.setEnrollYear(studentDTO.getEnrollYear());
        student.setStatus(studentDTO.getStatus());
        student = studentRepository.save(student);
        
        return convertToDTO(student);
    }

    @Override
    @Transactional
    public StudentDTO update(Long id, StudentDTO studentDTO) {
        // 格式化学号
        String formattedStudentId = formatStudentId(studentDTO);
        
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        // 更新用户信息
        User user = student.getUser();
        user.setRealName(studentDTO.getRealName());
        user.setEmail(studentDTO.getEmail());
        user.setPhone(studentDTO.getPhone());
        user.setStatus(studentDTO.getStatus());
        user = userRepository.save(user);
        
        // 获取班级信息
        Class classInfo = classRepository.findById(studentDTO.getClassId())
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        // 更新学生信息
        student.setStudentId(formattedStudentId);
        student.setClassInfo(classInfo);
        student.setEnrollYear(studentDTO.getEnrollYear());
        student.setStatus(studentDTO.getStatus());
        student = studentRepository.save(student);
        
        return convertToDTO(student);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        // 删除用户
        userRepository.delete(student.getUser());
        
        // 删除学生
        studentRepository.delete(student);
    }

    // 格式化学号
    private String formatStudentId(StudentDTO studentDTO) {
        String studentId = studentDTO.getStudentId();
        
        // 如果已经是10位，直接返回
        if (studentId != null && studentId.length() == 10) {
            return studentId;
        }
        
        // 否则，根据年级、院系、班级和个人号生成10位学号
        // 假设学号格式为：年级(4位) + 院系编号(2位) + 班级(2位) + 个人号(2位)
        String year = String.valueOf(studentDTO.getEnrollYear());
        
        // 获取院系编号
        String departmentCode = "";
        if (studentDTO.getDepartmentId() != null) {
            // 根据院系ID获取院系编号
            Department department = departmentRepository.findById(studentDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("院系不存在"));
            departmentCode = department.getCode();
        } else {
            departmentCode = "00"; // 默认值
        }
        
        String classCode = String.format("%02d", studentDTO.getClassId());
        String personalCode = studentId != null && studentId.length() >= 2 ? 
                studentId.substring(studentId.length() - 2) : "01";
        
        return year + departmentCode + classCode + personalCode;
    }

    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setUserId(student.getUser().getId());
        dto.setUsername(student.getUser().getUsername());
        dto.setRealName(student.getUser().getRealName());
        dto.setStudentId(student.getStudentId());
        dto.setEmail(student.getUser().getEmail());
        dto.setPhone(student.getUser().getPhone());
        dto.setDepartmentId(student.getClassInfo().getMajor().getDepartment().getId());
        dto.setDepartmentName(student.getClassInfo().getMajor().getDepartment().getName());
        dto.setMajorId(student.getClassInfo().getMajor().getId());
        dto.setMajorName(student.getClassInfo().getMajor().getName());
        dto.setClassId(student.getClassInfo().getId());
        dto.setClassName(student.getClassInfo().getName());
        dto.setEnrollYear(student.getEnrollYear());
        dto.setStatus(student.getStatus());
        return dto;
    }
} 