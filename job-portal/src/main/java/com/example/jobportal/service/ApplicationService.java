package com.example.jobportal.service;

import com.example.jobportal.entity.Application;
import com.example.jobportal.repository.ApplicationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    public ApplicationService(ApplicationRepository applicationRepository, EmailService emailService) {
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
    }

    @Transactional
    @SuppressWarnings("null")
    public void applyForJob(Application application) {
        applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByApplicantId(Long applicantId) {
        return applicationRepository.findByApplicantId(applicantId);
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByJobId(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    @Transactional
    @SuppressWarnings("null")
    public void updateApplicationStatus(Long id, String status) {
        applicationRepository.findById(id).ifPresent(application -> {
            String oldStatus = application.getStatus();
            application.setStatus(status);
            applicationRepository.save(application);

            // Send notification if status changed to SHORTLISTED or REJECTED
            if (!status.equals(oldStatus)) {
                if ("SHORTLISTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {
                    emailService.sendJobStatusEmail(
                            application.getApplicant().getEmail(),
                            application.getApplicant().getName(),
                            application.getJob(),
                            status
                    );
                }
            }
        });
    }

    @Transactional(readOnly = true)
    public boolean hasAlreadyApplied(Long applicantId, Long jobId) {
        return applicationRepository.existsByApplicantIdAndJobId(applicantId, jobId);
    }

    @Transactional(readOnly = true)
    public List<Application> findAllApplications() {
        return applicationRepository.findAll(Sort.unsorted());
    }
}
