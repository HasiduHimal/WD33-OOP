package com.example.registration.repository;

import com.example.registration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email - used for login
    Optional<User> findByEmail(String email);

    // Check if email already exists
    boolean existsByEmail(String email);

    // Check if student ID already exists
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) > 0 FROM Student u WHERE u.studentId = ?1")
    boolean existsByStudentId(String studentId);
}

