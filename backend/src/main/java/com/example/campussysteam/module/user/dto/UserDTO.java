package com.example.campussysteam.module.user.dto;

import com.example.campussysteam.module.user.entity.Role;
import com.example.campussysteam.module.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Date;

/**
 * 用户数据传输对象
 */
@Data
@NoArgsConstructor
@Schema(description = "用户数据传输对象")
public class UserDTO {

    @Schema(description = "用户ID")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Schema(description = "用户名", required = true)
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "密码")
    private String password;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "用户角色：ADMIN-管理员，TEACHER-教师，STUDENT-学生，USER-普通用户")
    private String role;

    @Schema(description = "学号（学生特有）")
    private String studentId;

    @Schema(description = "教师编号（教师特有）")
    private String teacherId;

    @Schema(description = "院系")
    private String department;

    @Schema(description = "专业（学生特有）")
    private String major;

    @Schema(description = "班级（学生特有）")
    private String className;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "账号状态：ACTIVE-激活，INACTIVE-未激活，LOCKED-锁定")
    private String status;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 从User实体转换为UserDTO
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setRealName(user.getRealName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setRole(user.getRole().name());
        userDTO.setStudentId(user.getStudentId());
        userDTO.setTeacherId(user.getTeacherId());
        userDTO.setDepartment(user.getDepartment());
        userDTO.setMajor(user.getMajor());
        userDTO.setClassName(user.getClassName());
        userDTO.setStatus(user.getStatus());
        userDTO.setCreateTime(java.util.Date.from(user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        userDTO.setUpdateTime(java.util.Date.from(user.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        
        return userDTO;
    }

    /**
     * 转换为User实体
     */
    public User toEntity() {
        User user = new User();
        user.setId(this.getId());
        user.setUsername(this.getUsername());
        user.setRealName(this.getRealName());
        user.setPassword(this.getPassword());
        user.setEmail(this.getEmail());
        user.setPhone(this.getPhone());
        user.setRole(Role.valueOf(this.getRole()));
        user.setStudentId(this.getStudentId());
        user.setTeacherId(this.getTeacherId());
        user.setDepartment(this.getDepartment());
        user.setMajor(this.getMajor());
        user.setClassName(this.getClassName());
        user.setStatus(this.getStatus());
        
        return user;
    }
} 