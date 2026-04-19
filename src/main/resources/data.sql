-- 회사 정보
INSERT IGNORE INTO company (company_name, company_address_en, company_address_kr, company_tel, company_fax, company_email, company_website)
VALUES ('한화솔루션', '86 Cheonggyecheon-ro, Jung-gu, Seoul', '서울특별시 중구 청계천로 86', '02-729-2700', '02-729-2799', 'contact@hanwha.com', 'https://www.hanwhasolutions.com');

-- 부서
INSERT IGNORE INTO departments (department_id, department_name, created_at) VALUES (1, '영업부', NOW());
INSERT IGNORE INTO departments (department_id, department_name, created_at) VALUES (2, '생산부', NOW());
INSERT IGNORE INTO departments (department_id, department_name, created_at) VALUES (3, '출하부', NOW());
INSERT IGNORE INTO departments (department_id, department_name, created_at) VALUES (4, '경영지원부', NOW());

-- 팀 (부서별 2팀)
INSERT IGNORE INTO teams (team_id, team_name, department_id, created_at) VALUES (1, '영업1팀', 1, NOW());
INSERT IGNORE INTO teams (team_id, team_name, department_id, created_at) VALUES (2, '영업2팀', 1, NOW());
INSERT IGNORE INTO teams (team_id, team_name, department_id, created_at) VALUES (3, '생산1팀', 2, NOW());
INSERT IGNORE INTO teams (team_id, team_name, department_id, created_at) VALUES (4, '생산2팀', 2, NOW());
INSERT IGNORE INTO teams (team_id, team_name, department_id, created_at) VALUES (5, '출하1팀', 3, NOW());
INSERT IGNORE INTO teams (team_id, team_name, department_id, created_at) VALUES (6, '출하2팀', 3, NOW());
INSERT IGNORE INTO teams (team_id, team_name, department_id, created_at) VALUES (7, '경영지원1팀', 4, NOW());
INSERT IGNORE INTO teams (team_id, team_name, department_id, created_at) VALUES (8, '경영지원2팀', 4, NOW());

-- 직급 (level 1 = 결재 권한 있음 / 3 = 일반 팀원). seed_dml.sql 과 동일 규칙.
INSERT IGNORE INTO positions (position_name, position_level, created_at) VALUES ('팀장', 1, NOW());
INSERT IGNORE INTO positions (position_name, position_level, created_at) VALUES ('팀원', 3, NOW());

-- 사용자 (비밀번호: password123 -> BCrypt 해시)
-- 사번 형식: YYMMDDNN (입사일+순번)

-- ADMIN (경영지원1팀 팀장)
INSERT IGNORE INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, team_id, position_id, created_at, updated_at)
VALUES ('26030101', '최관리', 'admin@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'admin', 'active', 7, 1, NOW(), NOW());

-- 영업부 (영업1팀)
INSERT IGNORE INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, team_id, position_id, created_at, updated_at)
VALUES ('26030102', '김영업', 'kim.sales@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'sales', 'active', 1, 2, NOW(), NOW());

INSERT IGNORE INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, team_id, position_id, created_at, updated_at)
VALUES ('26030103', '이영업', 'lee.sales@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'sales', 'active', 1, 1, NOW(), NOW());

-- 생산부 (생산1팀)
INSERT IGNORE INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, team_id, position_id, created_at, updated_at)
VALUES ('26030201', '박생산', 'park.prod@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'production', 'active', 3, 2, NOW(), NOW());

INSERT IGNORE INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, team_id, position_id, created_at, updated_at)
VALUES ('26030202', '최생산', 'choi.prod@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'production', 'active', 3, 1, NOW(), NOW());

-- 출하부 (출하1팀)
INSERT IGNORE INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, team_id, position_id, created_at, updated_at)
VALUES ('26030301', '정출하', 'jung.ship@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'shipping', 'active', 5, 2, NOW(), NOW());

-- 퇴직자 (영업1팀)
INSERT IGNORE INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, team_id, position_id, created_at, updated_at)
VALUES ('26030104', '강퇴직', 'kang.quit@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'sales', 'retired', 1, 2, NOW(), NOW());

-- 휴직자 (생산1팀)
INSERT IGNORE INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, team_id, position_id, created_at, updated_at)
VALUES ('26030203', '윤휴직', 'yoon.leave@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'production', 'on_leave', 3, 2, NOW(), NOW());
