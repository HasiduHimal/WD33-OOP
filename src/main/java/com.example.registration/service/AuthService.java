package com.example.registration.service;

import com.example.registration.model.Admin;
import com.example.registration.model.Student;
import com.example.registration.model.User;
import com.example.registration.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register a new student
    public Student registerStudent(String name, String email, String password, String studentId) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // Check if student ID already exists
        if (userRepository.existsByStudentId(studentId)) {
            throw new RuntimeException("Student ID '" + studentId + "' is already registered");
        }

        // Create new student - Inheritance shown here
        Student student = new Student(name, email, password, studentId);
        return userRepository.save(student);
    }

    // Login - returns user if credentials match
    public Optional<User> login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return user;
        }
        return Optional.empty();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}

