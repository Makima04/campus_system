-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_system;

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    role_level INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) NOT NULL UNIQUE,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_level INT NOT NULL DEFAULT 0,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(id),
    FOREIGN KEY (permission_id) REFERENCES permission(id),
    UNIQUE KEY unique_role_permission (role_id, permission_id)
);

-- 院系表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 专业表
CREATE TABLE IF NOT EXISTS major (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department(id),
    UNIQUE KEY unique_major_code (code)
);

-- 班级表
CREATE TABLE IF NOT EXISTS class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    grade INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (major_id) REFERENCES major(id),
    UNIQUE KEY unique_class_name_grade (name, grade)
);

-- 基础用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    role VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    class_id BIGINT NOT NULL,
    enroll_year INT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (class_id) REFERENCES class(id)
);

-- 学生专业变更历史表
CREATE TABLE IF NOT EXISTS student_major_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    old_major_id BIGINT,
    new_major_id BIGINT NOT NULL,
    old_class_id BIGINT,
    new_class_id BIGINT NOT NULL,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (old_major_id) REFERENCES major(id),
    FOREIGN KEY (new_major_id) REFERENCES major(id),
    FOREIGN KEY (old_class_id) REFERENCES class(id),
    FOREIGN KEY (new_class_id) REFERENCES class(id)
);

-- 教师表
CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    teacher_id VARCHAR(20) NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    title VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (department_id) REFERENCES department(id)
);

-- 饭卡表
CREATE TABLE IF NOT EXISTS meal_card (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 饭卡交易记录表
CREATE TABLE IF NOT EXISTS meal_card_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meal_card_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (meal_card_id) REFERENCES meal_card(id)
);

-- 校园通知表
CREATE TABLE IF NOT EXISTS notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    publisher_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES user(id)
);

-- 图书表
CREATE TABLE IF NOT EXISTS book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    isbn VARCHAR(20) UNIQUE,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 借阅记录表
CREATE TABLE IF NOT EXISTS borrow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'BORROWED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(50) NOT NULL,
    department_id BIGINT,
    teacher_id BIGINT NOT NULL,
    teacher_name VARCHAR(50),
    credits DOUBLE NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    class_time VARCHAR(100),
    classroom VARCHAR(100),
    weeks VARCHAR(50),
    sections VARCHAR(50),
    capacity INT NOT NULL,
    selected_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES department(id),
    FOREIGN KEY (teacher_id) REFERENCES user(id)
);

-- 选课记录表
CREATE TABLE IF NOT EXISTS course_selection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'SELECTED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES course(id),
    FOREIGN KEY (student_id) REFERENCES user(id),
    UNIQUE KEY unique_course_student (course_id, student_id)
);

-- 插入基础角色数据
INSERT INTO role (role_name, description, role_level) VALUES
('ROLE_SYSTEM_ADMIN', 'System Administrator with all permissions', 100),
('ROLE_NOTICE_ADMIN', 'Notice Administrator responsible for campus notices', 80),
('ROLE_LIBRARY_ADMIN', 'Library Administrator responsible for book management', 80),
('ROLE_TEACHER', 'Teacher responsible for course management', 60),
('ROLE_STUDENT', 'Student with basic permissions', 40);

-- 插入基础权限数据
INSERT INTO permission (permission_name, permission_code, permission_level, description) VALUES
-- User Management Permissions (Level 100)
('User Management', 'USER_MANAGE', 100, 'Manage user information'),
('Role Management', 'ROLE_MANAGE', 100, 'Manage role information'),
('Permission Management', 'PERMISSION_MANAGE', 100, 'Manage permission information'),

-- Meal Card Management Permissions (Level 80)
('Meal Card Management', 'MEAL_CARD_MANAGE', 80, 'Manage meal card information'),
('Meal Card Recharge', 'MEAL_CARD_RECHARGE', 80, 'Recharge meal card'),
('Meal Card Consume', 'MEAL_CARD_CONSUME', 40, 'Consume meal card'),
('Meal Card Query', 'MEAL_CARD_QUERY', 40, 'Query meal card information'),

-- Notice Management Permissions (Level 80)
('Notice Publish', 'NOTICE_PUBLISH', 80, 'Publish campus notices'),
('Notice Management', 'NOTICE_MANAGE', 80, 'Manage campus notices'),
('Notice View', 'NOTICE_VIEW', 40, 'View campus notices'),

-- Book Management Permissions (Level 80)
('Book Management', 'BOOK_MANAGE', 80, 'Manage book information'),
('Book Borrow', 'BOOK_BORROW', 40, 'Borrow books'),
('Book Return', 'BOOK_RETURN', 40, 'Return books'),
('Book Query', 'BOOK_QUERY', 40, 'Query book information'),

-- Course Management Permissions (Level 60)
('Course Management', 'COURSE_MANAGE', 60, 'Manage course information'),
('Course Create', 'COURSE_CREATE', 60, 'Create courses'),
('Course Update', 'COURSE_UPDATE', 60, 'Update course information'),
('Course Selection Management', 'COURSE_SELECTION_MANAGE', 60, 'Manage course selection'),
('Course Select', 'COURSE_SELECT', 40, 'Select courses'),
('Course Drop', 'COURSE_DROP', 40, 'Drop courses');

-- 为系统管理员分配所有权限
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.role_name = 'ROLE_SYSTEM_ADMIN';

-- 为通知管理员分配通知相关权限
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.role_name = 'ROLE_NOTICE_ADMIN'
AND p.permission_code IN ('NOTICE_PUBLISH', 'NOTICE_MANAGE', 'NOTICE_VIEW');

-- 为图书管理员分配图书相关权限
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.role_name = 'ROLE_LIBRARY_ADMIN'
AND p.permission_code IN ('BOOK_MANAGE', 'BOOK_BORROW', 'BOOK_RETURN', 'BOOK_QUERY');

-- 为教师分配课程相关权限
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.role_name = 'ROLE_TEACHER'
AND p.permission_code IN ('COURSE_MANAGE', 'COURSE_CREATE', 'COURSE_UPDATE', 'COURSE_SELECTION_MANAGE');

-- 为学生分配基本权限
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r, permission p
WHERE r.role_name = 'ROLE_STUDENT'
AND p.permission_code IN ('MEAL_CARD_CONSUME', 'MEAL_CARD_QUERY', 'NOTICE_VIEW', 
                        'BOOK_BORROW', 'BOOK_RETURN', 'BOOK_QUERY', 
                        'COURSE_SELECT', 'COURSE_DROP');

-- 插入基础院系数据
INSERT INTO department (name, code, description) VALUES
('计算机科学与技术学院', '01', '计算机科学与技术学院'),
('经济管理学院', '02', '经济管理学院'),
('机械工程学院', '03', '机械工程学院'),
('外国语学院', '04', '外国语学院'),
('数学学院', '05', '数学学院'),
('物理学院', '06', '物理学院'),
('化学学院', '07', '化学学院'),
('生命科学学院', '08', '生命科学学院');

-- 插入基础专业数据
INSERT INTO major (department_id, name, code, description) VALUES
(1, '计算机科学与技术', '0101', '计算机科学与技术专业'),
(1, '软件工程', '0102', '软件工程专业'),
(1, '人工智能', '0103', '人工智能专业'),
(1, '数据科学与大数据技术', '0104', '数据科学与大数据技术专业'),
(2, '工商管理', '0201', '工商管理专业'),
(2, '市场营销', '0202', '市场营销专业'),
(2, '会计学', '0203', '会计学专业'),
(2, '金融学', '0204', '金融学专业'),
(3, '机械设计制造及其自动化', '0301', '机械设计制造及其自动化专业'),
(3, '工业设计', '0302', '工业设计专业'),
(4, '英语', '0401', '英语专业'),
(4, '日语', '0402', '日语专业'),
(5, '数学与应用数学', '0501', '数学与应用数学专业'),
(5, '统计学', '0502', '统计学专业'),
(6, '物理学', '0601', '物理学专业'),
(6, '应用物理学', '0602', '应用物理学专业'),
(7, '化学', '0701', '化学专业'),
(7, '应用化学', '0702', '应用化学专业'),
(8, '生物科学', '0801', '生物科学专业'),
(8, '生物技术', '0802', '生物技术专业');

-- 插入基础班级数据
INSERT INTO class (major_id, name, code, grade) VALUES
-- 计算机学院班级
(1, '计算机2101', '010101', 2021),  -- 计算机科学与技术专业01班
(1, '计算机2102', '010102', 2021),  -- 计算机科学与技术专业02班
(1, '计算机2201', '010103', 2022),  -- 计算机科学与技术专业03班
(1, '计算机2202', '010104', 2022),  -- 计算机科学与技术专业04班
(2, '软件2101', '010201', 2021),    -- 软件工程专业01班
(2, '软件2102', '010202', 2021),    -- 软件工程专业02班
(2, '软件2201', '010203', 2022),    -- 软件工程专业03班
(3, '人工智能2101', '010301', 2021), -- 人工智能专业01班
(3, '人工智能2201', '010302', 2022), -- 人工智能专业02班
(4, '数据科学2101', '010401', 2021), -- 数据科学专业01班
(4, '数据科学2201', '010402', 2022), -- 数据科学专业02班
-- 经济管理学院班级
(5, '工商2101', '020101', 2021),    -- 工商管理专业01班
(5, '工商2102', '020102', 2021),    -- 工商管理专业02班
(5, '工商2201', '020103', 2022),    -- 工商管理专业03班
(6, '营销2101', '020201', 2021),    -- 市场营销专业01班
(6, '营销2201', '020202', 2022),    -- 市场营销专业02班
(7, '会计2101', '020301', 2021),    -- 会计学专业01班
(7, '会计2201', '020302', 2022),    -- 会计学专业02班
(8, '金融2101', '020401', 2021),    -- 金融学专业01班
(8, '金融2201', '020402', 2022);    -- 金融学专业02班

-- 插入更多用户数据
INSERT INTO user (username, password, real_name, role, email, phone, status, created_at, updated_at) VALUES 
-- 管理员
('admin', '$2a$10$TpAR1GTsmw9t6YBIFdZtIOYjqmqpeRGtqWV6o0dfLb03/Ea67rODy', '管理员', 'ROLE_ADMIN', 'admin@example.com', '13800138000', 'ACTIVE', '2025-03-29 21:33:25', '2025-03-29 22:36:59'),
-- 学生
('makima', '$2a$10$TpAR1GTsmw9t6YBIFdZtIOYjqmqpeRGtqWV6o0dfLb03/Ea67rODy', '玛奇玛', 'ROLE_STUDENT', '2812876054@qq.com', '19934450081', 'ACTIVE', '2025-03-29 21:33:25', '2025-03-30 19:50:11'),
('denji', '$2a$10$TpAR1GTsmw9t6YBIFdZtIOYjqmqpeRGtqWV6o0dfLb03/Ea67rODy', '电次', 'ROLE_STUDENT', 'denji@example.com', '19934450082', 'ACTIVE', '2025-03-29 21:33:25', '2025-03-30 19:50:11'),
('power', '$2a$10$TpAR1GTsmw9t6YBIFdZtIOYjqmqpeRGtqWV6o0dfLb03/Ea67rODy', '帕瓦', 'ROLE_STUDENT', 'power@example.com', '19934450083', 'ACTIVE', '2025-03-29 21:33:25', '2025-03-30 19:50:11'),
-- 教师
('test', '$2a$10$iqpztBMpJ1SxUpz/zdZR3OLvWNXXMITfevoo1.O1pboc8zU0df5Sm', '测试教师', 'ROLE_TEACHER', '1@123', '1234567890', 'ACTIVE', '2025-03-30 20:26:03', '2025-04-05 11:51:08'),
('teacher1', '$2a$10$iqpztBMpJ1SxUpz/zdZR3OLvWNXXMITfevoo1.O1pboc8zU0df5Sm', '张教授', 'ROLE_TEACHER', 'teacher1@example.com', '1234567891', 'ACTIVE', '2025-03-30 20:26:03', '2025-04-05 11:51:08'),
('teacher2', '$2a$10$iqpztBMpJ1SxUpz/zdZR3OLvWNXXMITfevoo1.O1pboc8zU0df5Sm', '李教授', 'ROLE_TEACHER', 'teacher2@example.com', '1234567892', 'ACTIVE', '2025-03-30 20:26:03', '2025-04-05 11:51:08'),
('teacher3', '$2a$10$iqpztBMpJ1SxUpz/zdZR3OLvWNXXMITfevoo1.O1pboc8zU0df5Sm', '王教授', 'ROLE_TEACHER', 'teacher3@example.com', '1234567893', 'ACTIVE', '2025-03-30 20:26:03', '2025-04-05 11:51:08');

-- 插入学生数据
INSERT INTO student (user_id, student_id, class_id, enroll_year, status) VALUES
(2, '2021010101', 1, 2021, 'ACTIVE'),  -- 玛奇玛 (2021级计算机学院01班01号)
(3, '2021010102', 1, 2021, 'ACTIVE'),  -- 电次 (2021级计算机学院01班02号)
(4, '2021010201', 2, 2021, 'ACTIVE');  -- 帕瓦 (2021级计算机学院02班01号)

-- 插入学生专业变更历史数据（示例）
INSERT INTO student_major_history (student_id, old_major_id, new_major_id, old_class_id, new_class_id, reason) VALUES
(1, NULL, 1, NULL, 1, '入学分配');  -- 玛奇玛的初始专业分配记录
(2, NULL, 1, NULL, 1, '入学分配');  -- 电次的初始专业分配记录
(3, NULL, 2, NULL, 2, '入学分配');  -- 帕瓦的初始专业分配记录

-- 插入教师数据
INSERT INTO teacher (user_id, teacher_id, department_id, title, status) VALUES
(5, 'T2021001', 1, '副教授', 'ACTIVE'),  -- 测试教师
(6, 'T2021002', 1, '教授', 'ACTIVE'),    -- 张教授
(7, 'T2021003', 2, '教授', 'ACTIVE'),    -- 李教授
(8, 'T2021004', 3, '教授', 'ACTIVE');    -- 王教授

-- 插入课程数据
INSERT INTO course (course_code, course_name, department_id, teacher_id, teacher_name, credits, course_type, semester, class_time, classroom, weeks, sections, capacity, selected_count, status, description) VALUES
-- 计算机学院课程（使用教师的user_id）
('CS101', 'Java程序设计', 1, 5, '测试教师', 4.0, '必修', '2023-2024-2', '周一 1-2节', 'A101', '1-8,10-16', '1-2', 100, 0, '开放选课', 'Java语言基础与面向对象编程'),  -- 测试教师
('CS102', '数据结构', 1, 5, '测试教师', 4.0, '必修', '2023-2024-2', '周二 3-4节', 'A102', '1-8,10-16', '3-4', 100, 0, '开放选课', '基本数据结构与算法'),  -- 测试教师
('CS103', '计算机网络', 1, 6, '张教授', 3.0, '必修', '2023-2024-2', '周三 5-6节', 'A103', '1-8,10-16', '5-6', 80, 0, '开放选课', '计算机网络基础与协议'),  -- 张教授
('CS104', '操作系统', 1, 6, '张教授', 4.0, '必修', '2023-2024-2', '周四 7-8节', 'A104', '1-8,10-16', '7-8', 80, 0, '开放选课', '操作系统原理与实现'),  -- 张教授
-- 经济管理学院课程
('EM101', '管理学原理', 2, 7, '李教授', 3.0, '必修', '2023-2024-2', '周五 1-2节', 'B101', '1-8,10-16', '1-2', 120, 0, '开放选课', '管理学基本理论与方法'),  -- 李教授
('EM102', '市场营销学', 2, 7, '李教授', 3.0, '必修', '2023-2024-2', '周一 3-4节', 'B102', '1-8,10-16', '3-4', 120, 0, '开放选课', '市场营销理论与实践'),  -- 李教授
-- 机械工程学院课程
('ME101', '机械设计基础', 3, 8, '王教授', 4.0, '必修', '2023-2024-2', '周二 5-6节', 'C101', '1-8,10-16', '5-6', 90, 0, '开放选课', '机械设计基本原理'),  -- 王教授
('ME102', '工程制图', 3, 8, '王教授', 3.0, '必修', '2023-2024-2', '周三 7-8节', 'C102', '1-8,10-16', '7-8', 90, 0, '开放选课', '工程制图基础');  -- 王教授

-- 插入选课记录
INSERT INTO course_selection (course_id, student_id, status) VALUES
-- 玛奇玛的选课
(1, 2, 'SELECTED'),  -- Java程序设计
(2, 2, 'SELECTED'),  -- 数据结构
(3, 2, 'SELECTED'),  -- 计算机网络
-- 电次的选课
(1, 3, 'SELECTED'),  -- Java程序设计
(2, 3, 'SELECTED'),  -- 数据结构
(4, 3, 'SELECTED'),  -- 操作系统
-- 帕瓦的选课
(1, 4, 'SELECTED'),  -- Java程序设计
(3, 4, 'SELECTED'),  -- 计算机网络
(4, 4, 'SELECTED');  -- 操作系统

-- 重置AUTO_INCREMENT值，确保后续插入的ID接续当前最大ID
ALTER TABLE user AUTO_INCREMENT = 4;

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