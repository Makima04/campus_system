-- 系统日志表
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module VARCHAR(100) COMMENT '操作模块',
    type VARCHAR(50) COMMENT '操作类型',
    description VARCHAR(255) COMMENT '操作描述',
    method VARCHAR(255) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_method VARCHAR(20) COMMENT '请求方式',
    request_ip VARCHAR(50) COMMENT '请求IP',
    request_param TEXT COMMENT '请求参数',
    result TEXT COMMENT '请求结果',
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人用户名',
    status INT DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
    error_msg TEXT COMMENT '错误消息',
    cost_time BIGINT COMMENT '操作耗时',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_module (module),
    INDEX idx_type (type),
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志';

-- 添加注释
ALTER TABLE sys_log COMMENT '系统操作日志表'; 