-- 更新用户表中的角色名称
UPDATE user SET role = 'ROLE_ADMIN' WHERE role = 'ADMIN';
UPDATE user SET role = 'ROLE_STUDENT' WHERE role = 'STUDENT';
UPDATE user SET role = 'ROLE_TEACHER' WHERE role = 'TEACHER';
UPDATE user SET role = 'ROLE_NOTICE_ADMIN' WHERE role = 'NOTICE_ADMIN';
UPDATE user SET role = 'ROLE_LIBRARY_ADMIN' WHERE role = 'LIBRARY_ADMIN'; 