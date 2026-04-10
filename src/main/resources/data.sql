-- 회사 정보
INSERT INTO company (company_name, company_address_en, company_address_kr, company_tel, company_fax, company_email, company_website)
VALUES ('한화솔루션', '86 Cheonggyecheon-ro, Jung-gu, Seoul', '서울특별시 중구 청계천로 86', '02-729-2700', '02-729-2799', 'contact@hanwha.com', 'https://www.hanwhasolutions.com');

-- 부서
INSERT INTO departments (department_name, created_at) VALUES ('영업부', NOW());
INSERT INTO departments (department_name, created_at) VALUES ('생산부', NOW());
INSERT INTO departments (department_name, created_at) VALUES ('출하부', NOW());
INSERT INTO departments (department_name, created_at) VALUES ('경영지원부', NOW());

-- 직급 (level 1 = 결재 권한 있음)
INSERT INTO positions (position_name, position_level, created_at) VALUES ('팀장', 1, NOW());
INSERT INTO positions (position_name, position_level, created_at) VALUES ('팀원', 2, NOW());

-- 사용자 (비밀번호: password123 -> BCrypt 해시)
-- 사번 형식: YYMMDDNN (입사일+순번)

-- ADMIN (경영지원부 팀장)
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('26030101', '최관리', 'admin@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'admin', 'active', 4, 1, NOW(), NOW());

-- 영업부
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('26030102', '김영업', 'kim.sales@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'sales', 'active', 1, 2, NOW(), NOW());

INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('26030103', '이영업', 'lee.sales@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'sales', 'active', 1, 1, NOW(), NOW());

-- 생산부
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('26030201', '박생산', 'park.prod@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'production', 'active', 2, 2, NOW(), NOW());

INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('26030202', '최생산', 'choi.prod@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'production', 'active', 2, 1, NOW(), NOW());

-- 출하부
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('26030301', '정출하', 'jung.ship@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'shipping', 'active', 3, 2, NOW(), NOW());

-- 퇴직자 (영업부)
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('26030104', '강퇴직', 'kang.quit@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'sales', 'retired', 1, 2, NOW(), NOW());

-- 휴직자 (생산부)
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('26030203', '윤휴직', 'yoon.leave@hanwha.com', '$2a$10$D9NYuK6QaSwPFM0fnBN9gOHr8.xWmZyimaUxJUt7yiw69nDyQErXm', 'production', 'on_leave', 2, 2, NOW(), NOW());
