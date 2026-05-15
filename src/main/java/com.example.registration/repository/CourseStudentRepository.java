package com.example.registration.repository;

import com.example.registration.model.CourseStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Repository
public interface CourseStudentRepository extends JpaRepository<CourseStudent, Long> {

    // Find all course registrations for a student
    List<CourseStudent> findByStudentId(Long studentId);

    // Find all students registered in a course
    List<CourseStudent> findByCourseId(Long courseId);

    // Find specific registration
    Optional<CourseStudent> findByCourseIdAndStudentId(Long courseId, Long studentId);

    // Delete a registration
    @Modifying
    @Transactional
    void deleteByCourseIdAndStudentId(Long courseId, Long studentId);
}