package com.example.jobportal.controller;

import com.example.jobportal.dto.EmailRequest;
import com.example.jobportal.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationRestController {

    private final EmailService emailService;
    private final com.example.jobportal.repository.JobRepository jobRepository;

    public ApplicationRestController(EmailService emailService, com.example.jobportal.repository.JobRepository jobRepository) {
        this.emailService = emailService;
        this.jobRepository = jobRepository;
    }

    @PostMapping("/send-status")
    public ResponseEntity<Map<String, String>> sendStatusEmail(@Valid @RequestBody EmailRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            // Find job by title (jobRole in request)
            com.example.jobportal.entity.Job job = jobRepository.findByTitle(request.getJobRole());
            if (job == null) {
                // Fallback for demo: create a partial job if not found in DB
                job = new com.example.jobportal.entity.Job();
                job.setTitle(request.getJobRole());
                job.setCategory("N/A");
                job.setLocation("N/A");
                job.setExperience("N/A");
                job.setSalary("N/A");
                job.setSkills("N/A");
                job.setDescription("N/A");
            }

            emailService.sendJobStatusEmail(
                    request.getEmail(),
                    request.getCandidateName(),
                    job,
                    request.getStatus()
            );
            response.put("message", "Email sent successfully to " + request.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Failed to send email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
