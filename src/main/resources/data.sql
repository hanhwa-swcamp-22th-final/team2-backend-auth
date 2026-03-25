-- 회사 정보
INSERT INTO company (company_name, company_address_en, company_address_kr, company_tel, company_fax, company_email, company_website)
VALUES ('한화솔루션', '86 Cheonggyecheon-ro, Jung-gu, Seoul', '서울특별시 중구 청계천로 86', '02-729-2700', '02-729-2799', 'contact@hanwha.com', 'https://www.hanwhasolutions.com');

-- 부서
INSERT INTO departments (department_name, created_at) VALUES ('영업부', NOW());
INSERT INTO departments (department_name, created_at) VALUES ('생산부', NOW());
INSERT INTO departments (department_name, created_at) VALUES ('출하부', NOW());
INSERT INTO departments (department_name, created_at) VALUES ('경영지원부', NOW());

-- 직급 (level 1 = 결재 권한 있음)
INSERT INTO positions (position_name, position_level, created_at) VALUES ('사원', 5, NOW());
INSERT INTO positions (position_name, position_level, created_at) VALUES ('대리', 4, NOW());
INSERT INTO positions (position_name, position_level, created_at) VALUES ('과장', 3, NOW());
INSERT INTO positions (position_name, position_level, created_at) VALUES ('차장', 2, NOW());
INSERT INTO positions (position_name, position_level, created_at) VALUES ('부장', 1, NOW());

-- 사용자 (비밀번호: password123 -> BCrypt 해시)
-- ADMIN
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('EMP001', '관리자', 'admin@hanwha.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', '재직', 4, 5, NOW(), NOW());

-- 영업부
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('EMP002', '김영업', 'kim.sales@hanwha.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SALES', '재직', 1, 3, NOW(), NOW());

INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('EMP003', '이영업', 'lee.sales@hanwha.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SALES', '재직', 1, 1, NOW(), NOW());

-- 생산부
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('EMP004', '박생산', 'park.prod@hanwha.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PRODUCTION', '재직', 2, 2, NOW(), NOW());

INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('EMP005', '최생산', 'choi.prod@hanwha.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PRODUCTION', '재직', 2, 1, NOW(), NOW());

-- 출하부
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('EMP006', '정출하', 'jung.ship@hanwha.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SHIPPING', '재직', 3, 4, NOW(), NOW());

-- 퇴직자
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('EMP007', '강퇴직', 'kang.quit@hanwha.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SALES', '퇴직', 1, 1, NOW(), NOW());

-- 휴직자
INSERT INTO users (employee_no, user_name, user_email, user_pw, user_role, user_status, department_id, position_id, created_at, updated_at)
VALUES ('EMP008', '윤휴직', 'yoon.leave@hanwha.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PRODUCTION', '휴직', 2, 2, NOW(), NOW());
