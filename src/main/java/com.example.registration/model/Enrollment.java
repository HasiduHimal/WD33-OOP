package com.example.registration.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign keys stored as simple Long values
    private Long studentId;
    private Long courseId;

    private String status; // PENDING, APPROVED, REJECTED, DROPPED
    private LocalDateTime enrolledAt;

    // Composition: PaymentSlip is part of Enrollment
    @Embedded
    private PaymentSlip paymentSlip;

    // Transient fields for UI display - populated manually in service
    @Transient
    private String studentName;
    @Transient
    private String studentCode; // This is the STU... ID
    @Transient
    private String courseName;
    public Enrollment() {
        this.paymentSlip = new PaymentSlip();
        this.enrolledAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Enrollment(Long studentId, Long courseId) {
        this();
        this.studentId = studentId;
        this.courseId = courseId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public PaymentSlip getPaymentSlip() {
        return paymentSlip;
    }

    public void setPaymentSlip(PaymentSlip paymentSlip) {
        this.paymentSlip = paymentSlip;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}

