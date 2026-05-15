package com.example.registration.repository;

import com.example.registration.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Find all materials for a course
    List<Material> findByCourseId(Long courseId);
}

