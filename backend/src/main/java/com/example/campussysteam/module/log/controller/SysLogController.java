package com.example.campussysteam.module.log.controller;

import com.example.campussysteam.common.ApiResult;
import com.example.campussysteam.common.log.Log;
import com.example.campussysteam.common.log.LogService;
import com.example.campussysteam.common.log.OperationType;
import com.example.campussysteam.model.SysLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 系统日志控制器
 */
@RestController
@RequestMapping("/api/system/logs")
public class SysLogController {

    @Autowired
    private LogService logService;

    /**
     * 分页查询日志
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Log(module = "系统管理", type = OperationType.QUERY, description = "分页查询日志")
    public ApiResult<Page<SysLog>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Integer status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SysLog> logPage;
        
        if (status != null) {
            logPage = logService.findByStatus(status, pageable);
        } else if (username != null && !username.isEmpty()) {
            logPage = logService.findPageByUsername(username, pageable);
        } else if (module != null && !module.isEmpty()) {
            logPage = logService.findPageByModule(module, pageable);
        } else if (startTime != null && endTime != null) {
            logPage = logService.findPageByTime(startTime, endTime, pageable);
        } else {
            logPage = logService.findPage(pageable);
        }
        
        return ApiResult.success(logPage);
    }

    /**
     * 根据ID查询日志
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Log(module = "系统管理", type = OperationType.QUERY, description = "查询日志详情")
    public ApiResult<SysLog> getById(@PathVariable Long id) {
        SysLog sysLog = logService.findById(id);
        if (sysLog == null) {
            return ApiResult.error("日志不存在");
        }
        return ApiResult.success(sysLog);
    }
    
    /**
     * 查询归档日志
     */
    @GetMapping("/archived")
    @PreAuthorize("hasRole('ADMIN')")
    @Log(module = "系统管理", type = OperationType.QUERY, description = "查询归档日志")
    public ApiResult<Page<SysLog>> archivedLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SysLog> logPage = logService.findByStatus(SysLog.STATUS_ARCHIVED, pageable);
        
        return ApiResult.success(logPage);
    }
} 