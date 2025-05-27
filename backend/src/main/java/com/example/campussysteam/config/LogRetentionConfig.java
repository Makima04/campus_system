package com.example.campussysteam.config;

import com.example.campussysteam.model.SysLog;
import com.example.campussysteam.repository.SysLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * 日志保留策略配置
 * 自动归档过期日志，而不是删除
 */
@Configuration
@EnableScheduling
public class LogRetentionConfig {

    private static final Logger logger = LoggerFactory.getLogger(LogRetentionConfig.class);

    @Autowired
    private SysLogRepository sysLogRepository;

    @Value("${log.retention.days:365}")
    private int logRetentionDays;

    /**
     * 每天凌晨2点执行日志归档
     * 将超过保留期限的日志标记为已归档状态
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveOldLogs() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(logRetentionDays);
            logger.info("开始归档 {} 天前的日志", logRetentionDays);
            
            // 将超过保留期限的日志标记为已归档状态
            int archivedCount = sysLogRepository.archiveOldLogs(cutoffDate, SysLog.STATUS_ARCHIVED);
            
            logger.info("日志归档完成，共归档 {} 条日志记录", archivedCount);
        } catch (Exception e) {
            logger.error("日志归档过程中发生错误", e);
        }
    }
} 