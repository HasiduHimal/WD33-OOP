package com.example.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class RegistrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistrationApplication.class, args);

        System.out.println("============================================");
        System.out.println("Student Course Registration System Started!");
        System.out.println("============================================");
        System.out.println("Access the application at: http://localhost:8080");
        System.out.println("Default Admin: admin@university.edu / admin123");
        System.out.println("============================================");
    }
}

