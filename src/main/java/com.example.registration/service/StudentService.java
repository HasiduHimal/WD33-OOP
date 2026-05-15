package com.example.registration.service;

import com.example.registration.model.*;
import com.example.registration.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@Transactional
public class StudentService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final MaterialRepository materialRepository;
    private final FileStorageService fileStorageService;

    public StudentService(UserRepository userRepository,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          CourseStudentRepository courseStudentRepository,
                          MaterialRepository materialRepository,
                          FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.materialRepository = materialRepository;
        this.fileStorageService = fileStorageService;
    }

    // Get all courses - for browsing and searching
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // Search courses by title
    public List<Course> searchCourses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseRepository.findAll();
        }
        return courseRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // Get a single course by ID
    public Optional<Course> getCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    // Get student profile
    public Optional<Student> getStudentProfile(Long studentId) {
        Optional<User> user = userRepository.findById(studentId);
        if (user.isPresent() && user.get() instanceof Student) {
            Student student = (Student) user.get();
            // Manually populate Association: Student -> Courses
            List<CourseStudent> registrations = courseStudentRepository.findByStudentId(studentId);
            List<Course> courses = new ArrayList<>();
            for (CourseStudent reg : registrations) {
                courseRepository.findById(reg.getCourseId()).ifPresent(courses::add);
            }
            student.setCourses(courses);
            return Optional.of(student);
        }
        return Optional.empty();
    }

    // Update student profile
    public Student updateProfile(Long studentId, String name, String email) {
        Optional<User> user = userRepository.findById(studentId);
        if (user.isPresent() && user.get() instanceof Student) {
            Student student = (Student) user.get();
            student.setName(name);
            student.setEmail(email);
            return userRepository.save(student);
        }
        throw new RuntimeException("Student not found");
    }

    // Delete student account
    public void deleteStudent(Long studentId) {
        // Remove all enrollments
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        enrollmentRepository.deleteAll(enrollments);

        // Remove course associations
        List<CourseStudent> registrations = courseStudentRepository.findByStudentId(studentId);
        courseStudentRepository.deleteAll(registrations);

        // Remove user
        userRepository.deleteById(studentId);
    }

    // Enroll in a course with payment slip upload
    public Enrollment enrollCourse(Long studentId, Long courseId, MultipartFile paymentSlipFile) {
        // Check if already enrolled
        Optional<Enrollment> existing = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isPresent() && !existing.get().getStatus().equals("DROPPED") && !existing.get().getStatus().equals("REJECTED")) {
            throw new RuntimeException("Already enrolled in this course");
        }

        // Check course capacity
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        List<Enrollment> courseEnrollments = enrollmentRepository.findByCourseId(courseId);
        long activeCount = courseEnrollments.stream()
                .filter(e -> e.getStatus().equals("APPROVED"))
                .count();
        if (activeCount >= course.getCapacity()) {
            throw new RuntimeException("Course is full");
        }

        // Store payment slip file
        String filePath = fileStorageService.storeFile(paymentSlipFile);

        // Use existing enrollment if it exists (for REJECTED or DROPPED status)
        // Otherwise create new one. This avoids Duplicate Key error
        Enrollment enrollment;
        if (existing.isPresent()) {
            enrollment = existing.get();
            enrollment.setStatus("PENDING");
            enrollment.setEnrolledAt(java.time.LocalDateTime.now());
        } else {
            enrollment = new Enrollment(studentId, courseId);
        }

        enrollment.getPaymentSlip().setFilePath(filePath);
        Enrollment saved = enrollmentRepository.save(enrollment);
        populateEnrollmentNames(saved);

        // Add to join table for Association only if not already there
        if (!courseStudentRepository.findByCourseIdAndStudentId(courseId, studentId).isPresent()) {
            CourseStudent registration = new CourseStudent(courseId, studentId);
            courseStudentRepository.save(registration);
        }

        return saved;
    }

    // Get all enrollments for a student
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        for (Enrollment e : enrollments) {
            populateEnrollmentNames(e);
        }
        return enrollments;
    }

    // Helper to populate names
    private void populateEnrollmentNames(Enrollment e) {
        courseRepository.findById(e.getCourseId()).ifPresent(c -> e.setCourseName(c.getTitle()));
        userRepository.findById(e.getStudentId()).ifPresent(u -> {
            e.setStudentName(u.getName());
            if (u instanceof Student) {
                e.setStudentCode(((Student) u).getStudentId());
            }
        });
    }

    // Drop a course enrollment
    public void dropEnrollment(Long enrollmentId, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (!enrollment.getStudentId().equals(studentId)) {
            throw new RuntimeException("Not authorized to drop this enrollment");
        }

        enrollment.setStatus("DROPPED");
        enrollmentRepository.save(enrollment);

        // Remove from join table
        courseStudentRepository.deleteByCourseIdAndStudentId(
                enrollment.getCourseId(), enrollment.getStudentId());
    }

    // Access blocked until enrollment is approved
    public List<Material> getCourseMaterials(Long courseId, Long studentId) {
        Optional<Enrollment> enrollment = enrollmentRepository
                .findByStudentIdAndCourseId(studentId, courseId);
        if (enrollment.isEmpty() || !enrollment.get().getStatus().equals("APPROVED")) {
            throw new RuntimeException("Access denied: Enrollment not approved");
        }
        return materialRepository.findByCourseId(courseId);
    }

    // Update password
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(currentPassword)) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }
}
