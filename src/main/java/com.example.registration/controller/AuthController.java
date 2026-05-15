package com.example.registration.controller;

import com.example.registration.model.Admin;
import com.example.registration.model.Student;
import com.example.registration.model.User;
import com.example.registration.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Student registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            String studentId = request.get("studentId");

            Student student = authService.registerStudent(name, email, password, studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("studentId", student.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Login for both Student and Admin
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        Optional<User> user = authService.login(email, password);
        if (user.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.get().getId());
            response.put("name", user.get().getName());
            response.put("email", user.get().getEmail());

            if (user.get() instanceof Student) {
                Student s = (Student) user.get();
                response.put("role", "STUDENT");
                response.put("studentId", s.getStudentId());
            } else if (user.get() instanceof Admin) {
                Admin a = (Admin) user.get();
                response.put("role", "ADMIN");
                response.put("adminCode", a.getAdminCode());
            }

            return ResponseEntity.ok(response);
        }

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid email or password");
        return ResponseEntity.status(401).body(error);
    }
}
