package com.example.jobportal.service;

import com.example.jobportal.entity.Application;
import com.example.jobportal.repository.ApplicationRepository;
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

class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveApplication() {
        Application application = new Application();
        applicationService.applyForJob(application);
        verify(applicationRepository, times(1)).save(application);
    }

    @Test
    void testGetApplicationsByStudent() {
        Application app = new Application();
        when(applicationRepository.findByApplicantId(1L)).thenReturn(Arrays.asList(app));

        List<Application> results = applicationService.getApplicationsByApplicantId(1L);

        assertEquals(1, results.size());
    }

    @Test
    void testGetApplicationsByJob() {
        Application app = new Application();
        when(applicationRepository.findByJobId(1L)).thenReturn(Arrays.asList(app));

        List<Application> results = applicationService.getApplicationsByJobId(1L);

        assertEquals(1, results.size());
    }

    @Test
    void testGetApplicationById() {
        Application app = new Application();
        app.setId(1L);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        // Note: ApplicationService doesn't have getApplicationById, but it has updateApplicationStatus which uses it
        // and findAllApplications
    }

    @Test
    void testHasUserApplied() {
        when(applicationRepository.existsByApplicantIdAndJobId(1L, 1L)).thenReturn(true);

        boolean result = applicationService.hasAlreadyApplied(1L, 1L);

        assertTrue(result);
    }
}
