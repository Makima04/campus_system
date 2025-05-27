package com.example.campussysteam.common.log;

import com.example.campussysteam.model.SysLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志注解切面处理类
 */
@Aspect
@Component
public class LogAnnotationAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAnnotationAspect.class);
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点 - 所有带有@Log注解的方法
     */
    @Pointcut("@annotation(com.example.campussysteam.common.log.Log)")
    public void logPointCut() {}

    /**
     * 环绕通知处理
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        
        // 执行方法
        Object result = null;
        Exception exception = null;
        try {
            result = point.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 保存日志
            saveLog(point, result, exception, System.currentTimeMillis() - beginTime);
        }
        
        return result;
    }

    /**
     * 保存日志
     */
    private void saveLog(JoinPoint joinPoint, Object result, Exception ex, long time) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取注解信息
            Log logAnnotation = method.getAnnotation(Log.class);
            if (logAnnotation == null) {
                return;
            }
            
            // 创建日志实体
            SysLog sysLog = new SysLog();
            sysLog.setModule(logAnnotation.module());
            sysLog.setType(logAnnotation.type());
            sysLog.setDescription(logAnnotation.description());
            sysLog.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + method.getName());
            sysLog.setCostTime(time);
            
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                sysLog.setRequestUrl(request.getRequestURL().toString());
                sysLog.setRequestMethod(request.getMethod());
                sysLog.setRequestIp(getIpAddress(request));
                
                // 处理请求参数
                Object[] args = joinPoint.getArgs();
                try {
                    Map<String, Object> params = new HashMap<>();
                    for (int i = 0; i < args.length; i++) {
                        params.put("arg" + i, args[i]);
                    }
                    sysLog.setRequestParam(objectMapper.writeValueAsString(params));
                } catch (JsonProcessingException e) {
                    logger.error("处理请求参数失败", e);
                    sysLog.setRequestParam("处理请求参数失败: " + e.getMessage());
                }
            }
            
            // 处理返回结果
            if (result != null) {
                try {
                    sysLog.setResult(objectMapper.writeValueAsString(result));
                } catch (JsonProcessingException e) {
                    logger.error("处理返回结果失败", e);
                    sysLog.setResult("处理返回结果失败: " + e.getMessage());
                }
            }
            
            // 处理异常信息
            if (ex != null) {
                sysLog.setStatus(1);
                sysLog.setErrorMsg(ex.getMessage());
            } else {
                sysLog.setStatus(0);
            }
            
            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails = 
                            (org.springframework.security.core.userdetails.UserDetails) principal;
                    sysLog.setUsername(userDetails.getUsername());
                } else {
                    sysLog.setUsername(principal.toString());
                }
            }
            
            // 保存日志
            logService.save(sysLog);
        } catch (Exception e) {
            logger.error("保存操作日志失败", e);
        }
    }
    
    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 