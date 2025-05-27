-- 修改课程表结构
ALTER TABLE course
    MODIFY COLUMN course_name VARCHAR(50) NOT NULL,
    MODIFY COLUMN course_code VARCHAR(20) NOT NULL UNIQUE,
    MODIFY COLUMN course_type VARCHAR(20) NOT NULL,
    MODIFY COLUMN semester VARCHAR(20) NOT NULL,
    MODIFY COLUMN selected_count INT NOT NULL DEFAULT 0,
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    ADD COLUMN weeks VARCHAR(50) AFTER classroom,
    ADD COLUMN sections VARCHAR(50) AFTER weeks,
    MODIFY COLUMN capacity INT NOT NULL,
    MODIFY COLUMN class_time VARCHAR(100),
    MODIFY COLUMN classroom VARCHAR(100),
    MODIFY COLUMN description VARCHAR(500),
    MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    MODIFY COLUMN department_id INT;

-- 更新现有数据
UPDATE course SET 
    weeks = '1-8,10-16',
    sections = CASE 
        WHEN class_time LIKE '%1-2节%' THEN '1-2'
        WHEN class_time LIKE '%3-4节%' THEN '3-4'
        WHEN class_time LIKE '%5-6节%' THEN '5-6'
        WHEN class_time LIKE '%7-8节%' THEN '7-8'
        ELSE sections
    END;

UPDATE course 
SET department_id = CASE 
    WHEN course_code LIKE 'CS%' THEN 1  -- 计算机科学与技术学院
    WHEN course_code LIKE 'EM%' THEN 2  -- 经济管理学院
    WHEN course_code LIKE 'ME%' THEN 3  -- 机械工程学院
    ELSE NULL
END
WHERE department_id = 0;
