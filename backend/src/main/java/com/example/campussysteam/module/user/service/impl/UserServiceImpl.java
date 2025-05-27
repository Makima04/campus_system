package com.example.campussysteam.module.user.service.impl;

import com.example.campussysteam.module.user.entity.User;
import com.example.campussysteam.module.user.entity.Role;
import com.example.campussysteam.model.CacheResult;
import com.example.campussysteam.module.user.repository.UserRepository;
import com.example.campussysteam.security.JwtService;
import com.example.campussysteam.module.user.service.UserService;
import com.example.campussysteam.common.util.CacheContext;
import com.example.campussysteam.module.user.dto.UserDTO;
import com.example.campussysteam.module.user.dto.PasswordChangeDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.campussysteam.module.redis.service.RedisService;
import com.example.campussysteam.common.util.CryptoUtil;
import com.example.campussysteam.common.util.PasswordUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;
    private final PasswordUtil passwordUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }

    @Override
    public User createUser(User user) {
        try {
            // 对密码进行处理
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordUtil.encryptPassword(user.getPassword()));
            } else {
                logger.warn("未提供密码");
            }
            
            // 设置用户状态为ACTIVE
            user.setStatus("ACTIVE");
            
            // 设置创建和更新时间
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            
            User savedUser = userRepository.save(user);
            logger.info("用户保存成功，ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("保存用户失败: {}", e.getMessage());
            logger.error("异常堆栈:", e);
            throw new RuntimeException("保存用户失败: " + e.getMessage(), e);
        }
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public String authenticate(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        
        UserDetails user = loadUserByUsername(username);
        return jwtService.generateToken(user);
    }

    @Override
    public CacheResult<User> findByIdWithCacheInfo(Long id) {
        String cacheKey = "user:" + id;
        String lockKey = "lock:user:" + id;
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;

        // 使用缓存击穿保护方法获取用户数据
        User user = redisService.getWithCachePenetrationProtection(
                cacheKey, 
                lockKey,
                () -> userRepository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("用户不存在")),
                30 * 60 // 30分钟
        );
        
        // 判断是否命中缓存（检查处理时间，如果时间很短，认为是缓存命中）
        long processingTime = System.currentTimeMillis() - startTime;
        cacheHit = processingTime < 50; // 假设小于50ms的操作是缓存命中
        
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(30);

        return CacheResult.<User>builder()
                .data(user)
                .cacheHit(cacheHit)
                .processingTime(processingTime)
                .expirationTime(expirationTime)
                .build();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        CacheResult<User> result = findByIdWithCacheInfo(id);
        CacheContext<User> context = CacheContext.of(
            result.getData(),
            result.isCacheHit(),
            result.getProcessingTime(),
            LocalDateTime.now().plusSeconds(30 * 60)
        );
        return context.getData();
    }

    @Override
    public User getUserByUsername(String username) {
        return findByUsername(username);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        logger.info("更新用户信息，ID={}, 用户名={}", id, existingUser.getUsername());
        
        // 更新基本信息
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        
        // 根据角色更新特定字段
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        
        // 更新学生特有信息
        if (user.getStudentId() != null) {
            existingUser.setStudentId(user.getStudentId());
        }
        if (user.getMajor() != null) {
            existingUser.setMajor(user.getMajor());
        }
        if (user.getClassName() != null) {
            existingUser.setClassName(user.getClassName());
        }
        
        // 更新教师特有信息
        if (user.getTeacherId() != null) {
            existingUser.setTeacherId(user.getTeacherId());
        }
        
        // 更新通用字段
        if (user.getDepartment() != null) {
            existingUser.setDepartment(user.getDepartment());
        }
        if (user.getEnrollYear() != null) {
            existingUser.setEnrollYear(user.getEnrollYear());
        }
        
        // 管理员可以更新状态
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        
        // 如果提供了密码，则更新密码
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordUtil.encryptPassword(user.getPassword()));
            logger.info("用户密码已更新，ID={}", id);
        }
        
        // 更新时间
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新后的用户
        User updatedUser = userRepository.save(existingUser);
        logger.info("用户更新成功，ID={}", id);
        
        return updatedUser;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        redisTemplate.delete("user:" + id);
    }

    @Override
    public void clearCache() {
        Set<String> keys = redisTemplate.keys("user:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<UserDTO> getTeacherList(String username, String realName, String teacherId, String department, int page, int size) {
        // 实现教师列表查询逻辑
        logger.info("获取教师列表: username={}, realName={}, teacherId={}, department={}, page={}, size={}", 
                username, realName, teacherId, department, page, size);
        
        // 这里应该是查询逻辑，简化起见，先返回空列表
        return List.of();
    }

    @Override
    public UserDTO getCurrentUserInfo() {
        // 获取当前用户信息的实现
        logger.info("获取当前用户信息");
        return new UserDTO(); // 简化实现
    }
    
    @Override
    public UserDTO updateProfile(UserDTO userDTO) {
        // 更新用户个人资料的实现
        logger.info("更新用户个人资料: {}", userDTO.getUsername());
        return userDTO; // 简化实现
    }
    
    @Override
    public void changePassword(PasswordChangeDTO passwordChangeDTO) {
        try {
            // 获取当前用户
            User user = findByUsername(passwordChangeDTO.getUsername());
            
            // 验证旧密码
            if (!passwordUtil.verifyPassword(passwordChangeDTO.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("旧密码不正确");
            }
            
            // 更新新密码
            user.setPassword(passwordUtil.encryptPassword(passwordChangeDTO.getNewPassword()));
            userRepository.save(user);
            
            logger.info("用户密码修改成功: username={}", passwordChangeDTO.getUsername());
        } catch (Exception e) {
            logger.error("修改密码失败: {}", e.getMessage());
            throw new RuntimeException("修改密码失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<UserDTO> getUserList(String username, String role, int page, int size) {
        logger.info("获取用户列表: username={}, role={}, page={}, size={}", 
                username, role, page, size);
        
        try {
            // 创建分页请求
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
            
            // 根据条件查询用户
            List<User> users;
            if (username != null && !username.isEmpty()) {
                users = userRepository.findByUsernameContaining(username, pageRequest).getContent();
            } else if (role != null && !role.isEmpty()) {
                try {
                    Role roleEnum = Role.valueOf(role);
                    users = userRepository.findByRole(roleEnum, pageRequest).getContent();
                } catch (IllegalArgumentException e) {
                    logger.error("无效的角色值: {}", role);
                    return List.of();
                }
            } else {
                users = userRepository.findAll(pageRequest).getContent();
            }
            
            // 转换为DTO
            return users.stream()
                    .map(UserDTO::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取用户列表失败: {}", e.getMessage());
            throw new RuntimeException("获取用户列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // 创建新用户的实现
        logger.info("创建新用户: {}", userDTO.getUsername());
        return userDTO; // 简化实现
    }
    
    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        // 更新用户信息的实现
        logger.info("更新用户信息: id={}, username={}", id, userDTO.getUsername());
        return userDTO; // 简化实现
    }
    
    @Override
    public List<UserDTO> getStudentList(String username, String realName, String studentId, String className, int page, int size) {
        // 获取学生列表的实现
        logger.info("获取学生列表: username={}, realName={}, studentId={}, className={}, page={}, size={}", 
                username, realName, studentId, className, page, size);
        return List.of(); // 简化实现
    }
} 