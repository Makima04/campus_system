package com.example.campussysteam.common.log;

import com.example.campussysteam.model.SysLog;
import com.example.campussysteam.repository.SysLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 日志服务实现类
 */
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private SysLogRepository sysLogRepository;

    @Override
    @Transactional
    public void save(SysLog sysLog) {
        sysLogRepository.save(sysLog);
    }

    @Override
    @Transactional
    public void saveBatch(List<SysLog> sysLogs) {
        sysLogRepository.saveAll(sysLogs);
    }

    @Override
    public SysLog findById(Long id) {
        Optional<SysLog> optionalSysLog = sysLogRepository.findById(id);
        return optionalSysLog.orElse(null);
    }

    @Override
    public List<SysLog> findAll() {
        return sysLogRepository.findAll();
    }

    @Override
    public Page<SysLog> findPage(Pageable pageable) {
        return sysLogRepository.findAll(pageable);
    }

    @Override
    public List<SysLog> findByType(OperationType type) {
        return sysLogRepository.findByType(type);
    }

    @Override
    public List<SysLog> findByUserId(Long userId) {
        return sysLogRepository.findByUserId(userId);
    }

    @Override
    public List<SysLog> findByStatus(Integer status) {
        return sysLogRepository.findByStatus(status);
    }

    @Override
    public Page<SysLog> findByStatus(Integer status, Pageable pageable) {
        return sysLogRepository.findByStatus(status, pageable);
    }

    @Override
    public List<SysLog> findByTime(LocalDateTime startTime, LocalDateTime endTime) {
        return sysLogRepository.findByCreateTimeBetween(startTime, endTime);
    }

    @Override
    public Page<SysLog> findPageByTime(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return sysLogRepository.findByCreateTimeBetween(startTime, endTime, pageable);
    }

    @Override
    public Page<SysLog> findPageByUsername(String username, Pageable pageable) {
        return sysLogRepository.findByUsernameContaining(username, pageable);
    }

    @Override
    public Page<SysLog> findPageByModule(String module, Pageable pageable) {
        return sysLogRepository.findByModuleContaining(module, pageable);
    }
} 