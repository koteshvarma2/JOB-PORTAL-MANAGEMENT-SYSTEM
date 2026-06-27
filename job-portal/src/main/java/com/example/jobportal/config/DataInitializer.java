package com.example.jobportal.config;

import com.example.jobportal.entity.User;
import com.example.jobportal.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Initializing users for database job_portal_db ===");

        // Fix all existing users with plain text passwords
        fixExistingUsers();

        // Ensure test users exist
        ensureTestUsersExist();

        System.out.println("=== User initialization complete ===");
    }

    private void fixExistingUsers() {
        System.out.println("Checking existing users for password encoding...");

        // Get all users and check their passwords
        userService.findAllUsers().forEach(user -> {
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                System.out.println("Fixing password for user: " + user.getUsername());
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userService.updateUser(user);
                System.out.println("✓ Password updated for: " + user.getUsername());
            } else {
                System.out.println("✓ Password already encoded for: " + user.getUsername());
            }
        });
    }

    private void ensureTestUsersExist() {
        // Create admin user if not exists
        if (userService.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setName("Administrator");
            admin.setEmail("admin@jobportal.com");
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            userService.saveUser(admin);
            System.out.println("✓ Admin user created!");
        } else {
            System.out.println("✓ Admin user already exists");
        }

        // Create a test user
        if (userService.findByUsername("student") == null) {
            User student = new User();
            student.setUsername("student");
            student.setPassword("student123");
            student.setName("Test Student");
            student.setEmail("student@jobportal.com");
            student.setRole("ROLE_STUDENT");
            student.setEnabled(true);
            userService.saveUser(student);
            System.out.println("✓ Student user created!");
        } else {
            System.out.println("✓ Student user already exists");
        }

        // Create a test employer user
        if (userService.findByUsername("employer") == null) {
            User employer = new User();
            employer.setUsername("employer");
            employer.setPassword("employer123");
            employer.setName("Test Employer");
            employer.setEmail("employer@jobportal.com");
            employer.setRole("ROLE_EMPLOYER");
            employer.setEnabled(true);
            userService.saveUser(employer);
            System.out.println("✓ Employer user created!");
        } else {
            System.out.println("✓ Employer user already exists");
        }
    }
}