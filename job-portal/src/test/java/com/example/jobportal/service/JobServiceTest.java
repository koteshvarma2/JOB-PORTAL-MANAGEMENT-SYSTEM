package com.example.jobportal.service;

import com.example.jobportal.entity.Job;
import com.example.jobportal.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveJob() {
        Job job = new Job();
        jobService.saveJob(job);
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    @SuppressWarnings("null")
    void testGetAllJobs() {
        Job job1 = new Job();
        Job job2 = new Job();
        when(jobRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(Arrays.asList(job1, job2));

        List<Job> jobs = jobService.findAllJobs();

        assertEquals(2, jobs.size());
    }

    @Test
    void testGetJobById() {
        Job job = new Job();
        job.setId(1L);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        Job found = jobService.findJobById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testDeleteJob() {
        jobService.deleteJob(1L);
        verify(jobRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSearchJobs() {
        Job job = new Job();
        when(jobRepository.searchByKeyword("java")).thenReturn(Arrays.asList(job));

        List<Job> results = jobService.searchJobs("java");

        assertEquals(1, results.size());
    }

    @Test
    void testGetJobsByEmployer() {
        Job job = new Job();
        when(jobRepository.findByPostedById(1L)).thenReturn(Arrays.asList(job));

        List<Job> jobs = jobService.getJobsByEmployerId(1L);

        assertEquals(1, jobs.size());
    }
}
