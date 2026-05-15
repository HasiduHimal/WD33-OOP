package com.example.registration.controller;

import com.example.registration.model.Course;
import com.example.registration.model.Enrollment;
import com.example.registration.model.Material;
import com.example.registration.model.Student;
import com.example.registration.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Get all courses (for browsing and searching)
    @GetMapping("/courses")
    public ResponseEntity<?> getCourses(@RequestParam(required = false) String search) {
        List<Course> courses = studentService.searchCourses(search);
        return ResponseEntity.ok(courses);
    }

    // Get single course
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<?> getCourse(@PathVariable Long courseId) {
        Optional<Course> course = studentService.getCourseById(courseId);
        if (course.isPresent()) {
            return ResponseEntity.ok(course.get());
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Course not found");
        return ResponseEntity.notFound().build();
    }

    // Get course materials
    // Requires studentId to verify approved enrollment
    @GetMapping("/courses/{courseId}/materials")
    public ResponseEntity<?> getCourseMaterials(@PathVariable Long courseId,
                                                @RequestParam Long studentId) {
        try {
            List<Material> materials = studentService.getCourseMaterials(courseId, studentId);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(403).body(error);
        }
    }

    // Get student profile
    @GetMapping("/profile/{studentId}")
    public ResponseEntity<?> getProfile(@PathVariable Long studentId) {
        Optional<Student> student = studentService.getStudentProfile(studentId);
        if (student.isPresent()) {
            return ResponseEntity.ok(student.get());
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Student not found");
        return ResponseEntity.notFound().build();
    }

    // Update student profile
    @PutMapping("/profile/{studentId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long studentId,
                                           @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            Student updated = studentService.updateProfile(studentId, name, email);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Delete student account
    @DeleteMapping("/profile/{studentId}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long studentId) {
        try {
            studentService.deleteStudent(studentId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Enroll in course with payment slip upload
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollCourse(@RequestParam Long studentId,
                                          @RequestParam Long courseId,
                                          @RequestParam("paymentSlip") MultipartFile paymentSlip) {
        try {
            Enrollment enrollment = studentService.enrollCourse(studentId, courseId, paymentSlip);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Enrollment request submitted");
            response.put("enrollmentId", enrollment.getId());
            response.put("status", enrollment.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get my enrollments
    @GetMapping("/enrollments/{studentId}")
    public ResponseEntity<?> getMyEnrollments(@PathVariable Long studentId) {
        List<Enrollment> enrollments = studentService.getStudentEnrollments(studentId);
        return ResponseEntity.ok(enrollments);
    }

    // Drop enrollment
    @PostMapping("/drop/{enrollmentId}")
    public ResponseEntity<?> dropEnrollment(@PathVariable Long enrollmentId,
                                            @RequestParam Long studentId) {
        try {
            studentService.dropEnrollment(enrollmentId, studentId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Course dropped successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update password
    @PutMapping("/profile/{studentId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long studentId,
                                            @RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            studentService.updatePassword(studentId, currentPassword, newPassword);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

