package com.example.registration.repository;

import com.example.registration.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Find all enrollments for a student
    List<Enrollment> findByStudentId(Long studentId);

    // Find enrollment by student and course
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // Find all enrollments for a course
    List<Enrollment> findByCourseId(Long courseId);

    // Find pending enrollments (for admin approval)
    List<Enrollment> findByStatus(String status);
}

