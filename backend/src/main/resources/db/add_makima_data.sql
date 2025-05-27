-- 以下代码已被注释，不再执行
/*
-- 添加学生makima的数据
-- 注意：这里假设makima用户已经存在，我们只需要添加相关的课程和成绩数据

-- 添加课程数据
INSERT INTO course (course_name, course_code, teacher_id, credits, capacity, 
                   course_type, semester, class_time, classroom, selected_count, 
                   status, description) 
VALUES 
('高等数学', 'MATH101', 1, 4.0, 100, 
 '必修', '2023-2024-2', '周一 1-2节', 'A101教室', 0, 
 '开放选课', '大学数学基础课程'),
('大学英语', 'ENG101', 1, 3.0, 80, 
 '必修', '2023-2024-2', '周二 3-4节', 'B201教室', 0, 
 '开放选课', '大学英语基础课程'),
('计算机基础', 'CS101', 1, 3.0, 60, 
 '必修', '2023-2024-2', '周三 5-6节', 'C301教室', 0, 
 '开放选课', '计算机入门课程'),
('数据结构', 'CS102', 1, 4.0, 50, 
 '必修', '2023-2024-2', '周四 7-8节', 'C302教室', 0, 
 '开放选课', '计算机核心课程'),
('操作系统', 'CS103', 1, 3.0, 40, 
 '必修', '2023-2024-2', '周五 9-10节', 'C303教室', 0, 
 '开放选课', '计算机系统课程');

-- 获取makima用户ID
SET @makima_id = (SELECT id FROM user WHERE username = 'makima');

-- 为makima添加选课记录
INSERT INTO course_selection (course_id, student_id, status, created_at, updated_at)
SELECT id, @makima_id, 'SELECTED', NOW(), NOW()
FROM course
WHERE course_code IN ('MATH101', 'ENG101', 'CS101', 'CS102', 'CS103');

-- 更新课程已选人数
UPDATE course c
JOIN course_selection cs ON c.id = cs.course_id
SET c.selected_count = c.selected_count + 1
WHERE cs.student_id = @makima_id;

-- 添加成绩数据
-- 注意：这里假设有一个成绩表，如果没有，需要先创建
CREATE TABLE IF NOT EXISTS grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES user(id),
    FOREIGN KEY (course_id) REFERENCES course(id),
    UNIQUE KEY unique_student_course (student_id, course_id)
);

-- 插入成绩数据
INSERT INTO grade (student_id, course_id, score, created_at, updated_at)
SELECT @makima_id, c.id, 
    CASE 
        WHEN c.course_code = 'MATH101' THEN 92.5
        WHEN c.course_code = 'ENG101' THEN 88.0
        WHEN c.course_code = 'CS101' THEN 95.0
        WHEN c.course_code = 'CS102' THEN 90.0
        WHEN c.course_code = 'CS103' THEN 87.5
    END,
    NOW(), NOW()
FROM course c
WHERE c.course_code IN ('MATH101', 'ENG101', 'CS101', 'CS102', 'CS103');

-- 添加图书借阅数据
-- 注意：这里假设有一个图书表和借阅记录表，如果没有，需要先创建
-- 添加图书数据
INSERT IGNORE INTO book (title, author, isbn, total_copies, available_copies, category, created_at, updated_at)
VALUES 
('Java编程思想', 'Bruce Eckel', '9787111213826', 5, 3, '计算机', NOW(), NOW()),
('算法导论', 'Thomas H. Cormen', '9787111187776', 3, 2, '计算机', NOW(), NOW()),
('计算机网络', 'Andrew S. Tanenbaum', '9787111374375', 4, 3, '计算机', NOW(), NOW());

-- 添加借阅记录
INSERT INTO borrow_record (book_id, user_id, borrow_date, due_date, return_date, status, created_at, updated_at)
SELECT 
    b.id, 
    @makima_id, 
    DATE_SUB(NOW(), INTERVAL 10 DAY), 
    DATE_ADD(NOW(), INTERVAL 20 DAY), 
    NULL, 
    'BORROWED', 
    NOW(), 
    NOW()
FROM book b
WHERE b.title = 'Java编程思想';

-- 更新图书可用数量
UPDATE book b
JOIN borrow_record br ON b.id = br.book_id
SET b.available_copies = b.available_copies - 1
WHERE br.user_id = @makima_id AND br.return_date IS NULL;

-- 添加通知数据
-- 注意：这里假设有一个通知表，如果没有，需要先创建
INSERT INTO notice (title, content, publisher_id, status, created_at, updated_at)
VALUES 
('期末考试安排', '各位同学，期末考试将于下月初开始，请做好准备。', 1, 'ACTIVE', NOW(), NOW()),
('图书馆开放时间调整', '因系统维护，图书馆将于本周六关闭一天。', 1, 'ACTIVE', NOW(), NOW());

-- 添加饭卡数据
-- 注意：这里假设有一个饭卡表，如果没有，需要先创建
INSERT INTO meal_card (user_id, balance, status, created_at, updated_at)
VALUES (@makima_id, 100.00, 'NORMAL', NOW(), NOW());

-- 添加饭卡交易记录
-- 注意：这里假设有一个饭卡交易记录表，如果没有，需要先创建
INSERT INTO meal_card_transaction (meal_card_id, amount, type, description, created_at)
SELECT 
    mc.id, 
    50.00, 
    'RECHARGE', 
    '充值', 
    NOW()
FROM meal_card mc
WHERE mc.user_id = @makima_id; 
*/ 