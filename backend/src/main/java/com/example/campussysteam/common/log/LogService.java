package com.example.campussysteam.common.log;

import com.example.campussysteam.model.SysLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志服务接口
 */
public interface LogService {
    
    /**
     * 保存日志
     */
    void save(SysLog sysLog);
    
    /**
     * 批量保存日志
     */
    void saveBatch(List<SysLog> sysLogs);
    
    /**
     * 根据ID查询日志
     */
    SysLog findById(Long id);
    
    /**
     * 查询所有日志
     */
    List<SysLog> findAll();
    
    /**
     * 分页查询日志
     */
    Page<SysLog> findPage(Pageable pageable);
    
    /**
     * 根据操作类型查询日志
     */
    List<SysLog> findByType(OperationType type);
    
    /**
     * 根据操作人ID查询日志
     */
    List<SysLog> findByUserId(Long userId);
    
    /**
     * 根据操作状态查询日志
     */
    List<SysLog> findByStatus(Integer status);
    
    /**
     * 分页查询指定状态的日志
     */
    Page<SysLog> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据时间范围查询日志
     */
    List<SysLog> findByTime(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分页查询时间范围内的日志
     */
    Page<SysLog> findPageByTime(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据用户名模糊查询日志
     */
    Page<SysLog> findPageByUsername(String username, Pageable pageable);
    
    /**
     * 根据模块名称模糊查询日志
     */
    Page<SysLog> findPageByModule(String module, Pageable pageable);
} 