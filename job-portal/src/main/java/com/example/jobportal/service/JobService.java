package com.example.jobportal.service;

import com.example.jobportal.entity.Job;
import com.example.jobportal.repository.JobRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    @SuppressWarnings("null")
    public void saveJob(Job job) {
        jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public List<Job> findAllJobs() {
        return jobRepository.findAll(Sort.unsorted());
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public Job findJobById(Long id) {
        Optional<Job> result = jobRepository.findById(id);
        return result.orElse(null);
    }

    @Transactional
    @SuppressWarnings("null")
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Job> searchJobs(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return jobRepository.searchByKeyword(keyword);
        }
        return findAllJobs();
    }

    @Transactional(readOnly = true)
    public List<Job> getJobsByEmployerId(Long employerId) {
        return jobRepository.findByPostedById(employerId);
    }
}
