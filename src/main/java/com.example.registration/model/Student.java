package com.example.registration.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {

    private String studentId;
    @Transient
    private List<Course> courses = new ArrayList<>();

    // Default constructor
    public Student() {
    }

    public Student(String name, String email, String password, String studentId) {
        super(name, email, password);
        this.studentId = studentId;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    // Helper method for Association
    public void addCourse(Course course) {
        this.courses.add(course);
    }

    public void removeCourse(Course course) {
        this.courses.remove(course);
    }
}
