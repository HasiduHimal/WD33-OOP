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
public class AdminService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final MaterialRepository materialRepository;
    private final FileStorageService fileStorageService;

    public AdminService(UserRepository userRepository,
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

    // Get all students
    public List<Student> getAllStudents() {
        List<User> users = userRepository.findAll();
        List<Student> students = new ArrayList<>();
        for (User user : users) {
            if (user instanceof Student) {
                Student s = (Student) user;
                // Populate student's courses for the count
                List<CourseStudent> registrations = courseStudentRepository.findByStudentId(s.getId());
                List<Course> enrolledCourses = new ArrayList<>();
                for (CourseStudent reg : registrations) {
                    courseRepository.findById(reg.getCourseId()).ifPresent(enrolledCourses::add);
                }
                s.setCourses(enrolledCourses);
                students.add(s);
            }
        }
        return students;
    }

    // Add a new course
    public Course addCourse(String title, String description, String schedule,
                            int capacity, Long adminId) {
        Course course = new Course(title, description, schedule, capacity, adminId);
        return courseRepository.save(course);
    }

    // Update course
    public Course updateCourse(Long courseId, String title, String description,
                               String schedule, int capacity) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setTitle(title);
        course.setDescription(description);
        course.setSchedule(schedule);
        course.setCapacity(capacity);
        return courseRepository.save(course);
    }

    // Delete course
    public void deleteCourse(Long courseId) {
        // Remove enrollments for this course
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        enrollmentRepository.deleteAll(enrollments);

        // Remove course-student associations
        List<CourseStudent> registrations = courseStudentRepository.findByCourseId(courseId);
        courseStudentRepository.deleteAll(registrations);

        // Remove materials
        List<Material> materials = materialRepository.findByCourseId(courseId);
        materialRepository.deleteAll(materials);

        // Remove course
        courseRepository.deleteById(courseId);
    }

    // Upload material for a course
    public Material uploadMaterial(Long courseId, String title, MultipartFile file) {
        String filePath = fileStorageService.storeFile(file);
        Material material = new Material(courseId, title, filePath);
        return materialRepository.save(material);
    }

    // Get all courses with materials populated
    public List<Course> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        for (Course course : courses) {
            // Manually populate Aggregation: Course -> Materials
            List<Material> materials = materialRepository.findByCourseId(course.getId());
            course.setMaterials(materials);

            // Manually populate Association: Course -> Students
            List<CourseStudent> registrations = courseStudentRepository.findByCourseId(course.getId());
            List<Student> students = new ArrayList<>();
            for (CourseStudent reg : registrations) {
                Optional<User> user = userRepository.findById(reg.getStudentId());
                if (user.isPresent() && user.get() instanceof Student) {
                    students.add((Student) user.get());
                }
            }
            course.setStudents(students);
        }
        return courses;
    }

    // Get pending enrollments
    public List<Enrollment> getPendingEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findByStatus("PENDING");
        for (Enrollment e : enrollments) {
            populateEnrollmentNames(e);
        }
        return enrollments;
    }

    private void populateEnrollmentNames(Enrollment e) {
        courseRepository.findById(e.getCourseId()).ifPresent(c -> e.setCourseName(c.getTitle()));
        userRepository.findById(e.getStudentId()).ifPresent(u -> {
            e.setStudentName(u.getName());
            if (u instanceof Student) {
                e.setStudentCode(((Student) u).getStudentId());
            }
        });
    }

    // Approve enrollment - Dependency shown here
    public Enrollment approveEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Dependency: Admin uses Enrollment object
        // We create a temporary Admin to call approve method
        // This demonstrates the dependency relationship
        Admin admin = new Admin();
        admin.approveEnrollment(enrollment);

        return enrollmentRepository.save(enrollment);
    }

    // Reject enrollment
    public Enrollment rejectEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        Admin admin = new Admin();
        admin.rejectEnrollment(enrollment);

        return enrollmentRepository.save(enrollment);
    }

    // Get course by ID with relationships
    public Optional<Course> getCourseById(Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            List<Material> materials = materialRepository.findByCourseId(courseId);
            course.setMaterials(materials);

            // Populate students for course view
            List<CourseStudent> registrations = courseStudentRepository.findByCourseId(courseId);
            List<Student> students = new ArrayList<>();
            for (CourseStudent reg : registrations) {
                userRepository.findById(reg.getStudentId()).ifPresent(u -> {
                    if (u instanceof Student) students.add((Student) u);
                });
            }
            course.setStudents(students);

            return Optional.of(course);
        }
        return Optional.empty();
    }

    // Delete a material
    public void deleteMaterial(Long materialId) {
        materialRepository.deleteById(materialId);
    }

    // Remove student from course
    public void removeStudentFromCourse(Long courseId, Long studentId) {
        // 1. Remove from join table (Association)
        courseStudentRepository.deleteByCourseIdAndStudentId(courseId, studentId);

        // 2. Mark enrollment as DROPPED so history is kept
        enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).ifPresent(e -> {
            e.setStatus("DROPPED");
            enrollmentRepository.save(e);
        });
    }
}

