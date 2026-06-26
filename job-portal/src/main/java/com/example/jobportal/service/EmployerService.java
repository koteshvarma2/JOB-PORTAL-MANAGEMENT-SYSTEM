package com.example.jobportal.service;

import com.example.jobportal.entity.Application;
import com.example.jobportal.entity.Job;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.repository.ApplicationRepository;
import com.example.jobportal.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployerService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    public EmployerService(JobRepository jobRepository, ApplicationRepository applicationRepository, EmailService emailService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public Job getJobById(Long jobId) {
        return jobRepository.findByIdWithPostedBy(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByJobId(Long jobId) {
        // First validate job exists
        getJobById(jobId);
        
        List<Application> applications = applicationRepository.findByJobId(jobId);
        
        // Add null check concept as requested
        if (applications == null) {
            return List.of();
        }
        
        return applications;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public void sendEmailConfirmation(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (application.getApplicant() == null || application.getJob() == null) {
            throw new IllegalStateException("Application data is incomplete");
        }

        emailService.sendJobStatusEmail(
                application.getApplicant().getEmail(),
                application.getApplicant().getName(),
                application.getJob(),
                application.getStatus()
        );
    }
}
