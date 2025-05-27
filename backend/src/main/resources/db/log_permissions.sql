-- 添加日志管理相关权限
INSERT INTO permission (permission_name, permission_code, permission_level, description) VALUES
('日志管理', 'LOG_MANAGE', 100, '管理系统日志'),
('日志查询', 'LOG_QUERY', 100, '查询系统日志');

-- 为系统管理员分配日志管理权限
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.role_name = 'ROLE_SYSTEM_ADMIN'
AND p.permission_code IN ('LOG_MANAGE', 'LOG_QUERY'); 