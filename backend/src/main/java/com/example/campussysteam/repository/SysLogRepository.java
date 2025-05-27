package com.example.campussysteam.repository;

import com.example.campussysteam.common.log.OperationType;
import com.example.campussysteam.model.SysLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统日志仓库接口
 */
@Repository
public interface SysLogRepository extends JpaRepository<SysLog, Long>, JpaSpecificationExecutor<SysLog> {
    
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
    List<SysLog> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分页查询日志
     */
    Page<SysLog> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据用户名模糊查询日志
     */
    Page<SysLog> findByUsernameContaining(String username, Pageable pageable);
    
    /**
     * 根据模块名称模糊查询日志
     */
    Page<SysLog> findByModuleContaining(String module, Pageable pageable);
    
    /**
     * 归档旧日志（将状态修改为已归档）
     * @param cutoffDate 截止日期
     * @param archivedStatus 归档状态值
     * @return 归档的记录数
     */
    @Modifying
    @Transactional
    @Query("UPDATE SysLog s SET s.status = :archivedStatus WHERE s.createTime < :cutoffDate AND s.status != :archivedStatus")
    int archiveOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("archivedStatus") Integer archivedStatus);
} 