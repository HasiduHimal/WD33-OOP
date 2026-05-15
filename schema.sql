-- ============================================
-- Student Course Registration System
-- Database Schema
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS course_registration;
USE course_registration;

-- ============================================
-- 1. Users Table
-- Supports Inheritance: User -> Student, Admin
-- Using SINGLE_TABLE strategy with discriminator
-- ============================================
DROP TABLE IF EXISTS materials;
DROP TABLE IF EXISTS course_students;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL,
                       user_type VARCHAR(20) NOT NULL,  -- Discriminator: STUDENT or ADMIN
                       student_id VARCHAR(50),          -- Only for Student
                       admin_code VARCHAR(50)           -- Only for Admin
);

-- ============================================
-- 2. Courses Table
-- ============================================
CREATE TABLE courses (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         title VARCHAR(200) NOT NULL,
                         description TEXT,
                         schedule VARCHAR(100),
                         capacity INT DEFAULT 30,
                         created_by_admin_id BIGINT
);

-- ============================================
-- 3. Enrollments Table
-- Composition: Enrollment owns PaymentSlip
-- ============================================
CREATE TABLE enrollments (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             student_id BIGINT NOT NULL,
                             course_id BIGINT NOT NULL,
                             status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, APPROVED, REJECTED, DROPPED
                             payment_slip_path VARCHAR(500),         -- Composition: PaymentSlip file path
                             enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE KEY unique_enrollment (student_id, course_id)
);

-- ============================================
-- 4. Materials Table
-- Aggregation: Materials exist independently
-- ============================================
CREATE TABLE materials (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           course_id BIGINT NOT NULL,
                           title VARCHAR(200) NOT NULL,
                           file_path VARCHAR(500)
);

-- ============================================
-- 5. Course_Students Table (Join Table)
-- Association: Student <-> Course (Many-to-Many)
-- ============================================
CREATE TABLE course_students (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 course_id BIGINT NOT NULL,
                                 student_id BIGINT NOT NULL,
                                 UNIQUE KEY unique_course_student (course_id, student_id)
);

-- ============================================
-- Insert Default Admin
-- ============================================
INSERT INTO users (name, email, password, user_type, admin_code)
VALUES ('System Admin', 'admin@university.edu', 'admin123', 'ADMIN', 'ADM001');
