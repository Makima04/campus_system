package com.example.campussysteam.module.user.service;

import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.model.CacheResult;
import com.example.campussysteam.module.user.dto.PasswordChangeDTO;
import com.example.campussysteam.module.user.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User save(User user);
    User findByUsername(String username);
    boolean existsByUsername(String username);
    String authenticate(String username, String password);
    CacheResult<User> findByIdWithCacheInfo(Long id);
    
    // 新增的方法
    List<User> getAllUsers();
    User getUserById(Long id);
    User getUserByUsername(String username);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    void clearCache();

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    UserDTO getCurrentUserInfo();

    /**
     * 更新用户个人资料
     * @param userDTO 用户数据
     * @return 更新后的用户信息
     */
    UserDTO updateProfile(UserDTO userDTO);

    /**
     * 修改密码
     * @param passwordChangeDTO 密码修改数据
     */
    void changePassword(PasswordChangeDTO passwordChangeDTO);

    /**
     * 获取用户列表
     * @param username 用户名
     * @param role 角色
     * @param page 页码
     * @param size 每页条数
     * @return 用户列表
     */
    List<UserDTO> getUserList(String username, String role, int page, int size);

    /**
     * 创建新用户
     * @param userDTO 用户数据
     * @return 创建的用户信息
     */
    UserDTO createUser(UserDTO userDTO);

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param userDTO 用户数据
     * @return 更新后的用户信息
     */
    UserDTO updateUser(Long id, UserDTO userDTO);

    /**
     * 获取学生列表
     * @param username 用户名
     * @param realName 真实姓名
     * @param studentId 学号
     * @param className 班级
     * @param page 页码
     * @param size 每页条数
     * @return 学生列表
     */
    List<UserDTO> getStudentList(String username, String realName, String studentId, String className, int page, int size);

    /**
     * 获取教师列表
     * @param username 用户名
     * @param realName 真实姓名
     * @param teacherId 教师工号
     * @param department 院系
     * @param page 页码
     * @param size 每页条数
     * @return 教师列表
     */
    List<UserDTO> getTeacherList(String username, String realName, String teacherId, String department, int page, int size);
} 