package com.example.registration.controller;

import com.example.registration.model.Course;
import com.example.registration.model.Enrollment;
import com.example.registration.model.Material;
import com.example.registration.model.Student;
import com.example.registration.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Get all students
    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents() {
        List<Student> students = adminService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    // Get all courses with materials and students
    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses() {
        List<Course> courses = adminService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    // Get single course
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<?> getCourse(@PathVariable Long courseId) {
        Optional<Course> course = adminService.getCourseById(courseId);
        if (course.isPresent()) {
            return ResponseEntity.ok(course.get());
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Course not found");
        return ResponseEntity.notFound().build();
    }

    // Add new course
    @PostMapping("/courses")
    public ResponseEntity<?> addCourse(@RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String schedule = (String) request.get("schedule");
            int capacity = (Integer) request.getOrDefault("capacity", 30);
            Long adminId = Long.valueOf(request.get("adminId").toString());

            Course course = adminService.addCourse(title, description, schedule, capacity, adminId);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update course
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId,
                                          @RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String schedule = (String) request.get("schedule");
            int capacity = (Integer) request.getOrDefault("capacity", 30);

            Course course = adminService.updateCourse(courseId, title, description, schedule, capacity);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Delete course
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        try {
            adminService.deleteCourse(courseId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Upload material
    @PostMapping("/materials")
    public ResponseEntity<?> uploadMaterial(@RequestParam Long courseId,
                                            @RequestParam String title,
                                            @RequestParam("file") MultipartFile file) {
        try {
            Material material = adminService.uploadMaterial(courseId, title, file);
            return ResponseEntity.ok(material);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get pending enrollments
    @GetMapping("/enrollments/pending")
    public ResponseEntity<?> getPendingEnrollments() {
        List<Enrollment> enrollments = adminService.getPendingEnrollments();
        return ResponseEntity.ok(enrollments);
    }

    // Approve enrollment
    @PostMapping("/enrollments/{enrollmentId}/approve")
    public ResponseEntity<?> approveEnrollment(@PathVariable Long enrollmentId) {
        try {
            Enrollment enrollment = adminService.approveEnrollment(enrollmentId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Enrollment approved");
            response.put("enrollment", enrollment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Reject enrollment
    @PostMapping("/enrollments/{enrollmentId}/reject")
    public ResponseEntity<?> rejectEnrollment(@PathVariable Long enrollmentId) {
        try {
            Enrollment enrollment = adminService.rejectEnrollment(enrollmentId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Enrollment rejected");
            response.put("enrollment", enrollment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Delete material
    @DeleteMapping("/materials/{materialId}")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long materialId) {
        try {
            adminService.deleteMaterial(materialId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Remove student from course
    @DeleteMapping("/courses/{courseId}/students/{studentId}")
    public ResponseEntity<?> removeStudent(@PathVariable Long courseId, @PathVariable Long studentId) {
        try {
            adminService.removeStudentFromCourse(courseId, studentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

