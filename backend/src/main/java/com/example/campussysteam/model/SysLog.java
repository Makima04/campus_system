package com.example.campussysteam.model;

import com.example.campussysteam.common.log.OperationType;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 系统日志实体类
 */
@Data
@Entity
@Table(name = "sys_log")
@EntityListeners(AuditingEntityListener.class)
public class SysLog {
    
    /**
     * 日志状态：正常
     */
    public static final int STATUS_NORMAL = 0;
    
    /**
     * 日志状态：异常
     */
    public static final int STATUS_ERROR = 1;
    
    /**
     * 日志状态：已归档
     */
    public static final int STATUS_ARCHIVED = 2;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    private OperationType type;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求方式
     */
    private String requestMethod;
    
    /**
     * 请求IP
     */
    private String requestIp;
    
    /**
     * 请求参数
     */
    @Column(length = 4000)
    private String requestParam;
    
    /**
     * 请求结果
     */
    @Column(length = 4000)
    private String result;
    
    /**
     * 操作人ID
     */
    private Long userId;
    
    /**
     * 操作人用户名
     */
    private String username;
    
    /**
     * 操作状态（0正常 1异常 2已归档）
     */
    private Integer status;
    
    /**
     * 错误消息
     */
    @Column(length = 4000)
    private String errorMsg;
    
    /**
     * 操作耗时
     */
    private Long costTime;
    
    /**
     * 操作时间
     */
    @CreatedDate
    private LocalDateTime createTime;
} 