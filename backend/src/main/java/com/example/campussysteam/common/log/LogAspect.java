package com.example.campussysteam.common.log;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.UUID;

/**
 * 日志切面，用于记录系统操作日志
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 定义切点 - 所有controller包下的方法
     */
    @Pointcut("execution(* com.example.campussysteam.module.*.controller.*.*(..))")
    public void controllerLog() {}

    /**
     * 前置通知：在方法执行前记录请求信息
     */
    @Before("controllerLog()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 生成请求ID
            String requestId = UUID.randomUUID().toString();
            request.setAttribute("requestId", requestId);
            
            // 记录请求日志
            logger.info("====== 请求开始 ======");
            logger.info("请求ID: {}", requestId);
            logger.info("请求URL: {}", request.getRequestURL().toString());
            logger.info("请求方法: {}", request.getMethod());
            logger.info("请求IP: {}", request.getRemoteAddr());
            logger.info("请求控制器: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), 
                    joinPoint.getSignature().getName());
            logger.info("请求参数: {}", Arrays.toString(joinPoint.getArgs()));
        }
    }

    /**
     * 环绕通知：记录方法执行时间
     */
    @Around("controllerLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long endTime = System.currentTimeMillis();
        logger.info("请求耗时: {}ms", endTime - startTime);
        return result;
    }

    /**
     * 后置通知：方法正常返回后执行
     */
    @AfterReturning(returning = "result", pointcut = "controllerLog()")
    public void doAfterReturning(Object result) {
        logger.info("请求返回: {}", result);
        logger.info("====== 请求结束 ======");
    }

    /**
     * 异常通知：方法抛出异常时执行
     */
    @AfterThrowing(throwing = "ex", pointcut = "controllerLog()")
    public void doAfterThrowing(Throwable ex) {
        logger.error("发生异常: {}", ex.getMessage());
        logger.error("异常堆栈: ", ex);
        logger.info("====== 请求异常结束 ======");
    }
} 