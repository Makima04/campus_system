package com.example.campussysteam.module.user.repository;

import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.module.user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据用户名模糊查询用户列表
     */
    Page<User> findByUsernameContaining(String username, Pageable pageable);
    
    /**
     * 根据角色查询用户列表
     */
    Page<User> findByRole(Role role, Pageable pageable);
} 