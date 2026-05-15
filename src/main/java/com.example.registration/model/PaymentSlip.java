package com.example.registration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
@Embeddable
public class PaymentSlip {

    @Column(name = "payment_slip_path")
    private String filePath;

    // Default constructor
    public PaymentSlip() {
    }

    public PaymentSlip(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

