package com.example.registration.model;

import jakarta.persistence.*;
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    private String adminCode;

    // Default constructor
    public Admin() {
    }

    public Admin(String name, String email, String password, String adminCode) {
        super(name, email, password);
        this.adminCode = adminCode;
    }
    public void approveEnrollment(Enrollment e) {
        e.setStatus("APPROVED");
    }

    public void rejectEnrollment(Enrollment e) {
        e.setStatus("REJECTED");
    }

    // Getters and Setters
    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }
}

