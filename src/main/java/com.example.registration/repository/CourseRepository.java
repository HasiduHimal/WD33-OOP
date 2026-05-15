package com.example.registration.repository;

import com.example.registration.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Search courses by title (case insensitive)
    List<Course> findByTitleContainingIgnoreCase(String title);
}
