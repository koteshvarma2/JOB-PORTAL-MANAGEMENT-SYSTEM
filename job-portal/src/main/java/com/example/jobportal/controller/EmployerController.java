package com.example.jobportal.controller;

import com.example.jobportal.entity.Application;
import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.User;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.EmployerService;
import com.example.jobportal.service.JobService;
import com.example.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final JobService jobService;
    private final UserService userService;
    private final ApplicationService applicationService;
    private final EmployerService employerService;

    public EmployerController(JobService jobService, UserService userService, 
                             ApplicationService applicationService, EmployerService employerService) {
        this.jobService = jobService;
        this.userService = userService;
        this.applicationService = applicationService;
        this.employerService = employerService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User employer = resolveUser(authentication);
        if (employer == null) return "redirect:/no-role";
        List<Job> jobs = jobService.getJobsByEmployerId(employer.getId());
        model.addAttribute("jobs", jobs);
        return "employer/dashboard";
    }

    @GetMapping("/job/new")
    public String newJobForm(Model model) {
        model.addAttribute("job", new Job());
        return "employer/post-job";
    }

    @PostMapping("/job/save")
    public String saveJob(@Valid @ModelAttribute("job") Job job, BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return "employer/post-job";
        }
        User employer = resolveUser(authentication);
        if (employer == null) return "redirect:/no-role";
        job.setPostedBy(employer);
        jobService.saveJob(job);
        return "redirect:/employer/dashboard";
    }

    @GetMapping("/job/edit/{id}")
    public String editJobForm(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Job job = jobService.findJobById(id);
        User employer = resolveUser(authentication);
        if (job == null || employer == null || !job.getPostedBy().getId().equals(employer.getId())) {
            return "redirect:/employer/dashboard";
        }
        model.addAttribute("job", job);
        return "employer/edit-job";
    }

    @PostMapping("/job/update/{id}")
    public String updateJob(@PathVariable("id") Long id, @Valid @ModelAttribute("job") Job job, BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return "employer/edit-job";
        }
        Job existingJob = jobService.findJobById(id);
        User employer = resolveUser(authentication);
        if (existingJob == null || employer == null || !existingJob.getPostedBy().getId().equals(employer.getId())) {
            return "redirect:/employer/dashboard";
        }
        existingJob.setTitle(job.getTitle());
        existingJob.setDescription(job.getDescription());
        existingJob.setSkills(job.getSkills());
        existingJob.setCategory(job.getCategory());
        existingJob.setLocation(job.getLocation());
        existingJob.setExperience(job.getExperience());
        existingJob.setSalary(job.getSalary());
        jobService.saveJob(existingJob);
        return "redirect:/employer/dashboard";
    }

    @PostMapping("/job/delete/{id}")
    public String deleteJob(@PathVariable("id") Long id, Authentication authentication) {
        Job job = jobService.findJobById(id);
        User employer = resolveUser(authentication);
        if (job != null && employer != null && job.getPostedBy().getId().equals(employer.getId())) {
            jobService.deleteJob(id);
        }
        return "redirect:/employer/dashboard";
    }

    @GetMapping("/job/{jobId}/applications")
    public String viewApplications(@PathVariable("jobId") Long jobId, Model model, Authentication authentication) {
        Job job = employerService.getJobById(jobId);
        User employer = resolveUser(authentication);
        
        if (employer == null || !job.getPostedBy().getId().equals(employer.getId())) {
            return "redirect:/employer/dashboard";
        }
        
        List<Application> applications = employerService.getApplicationsByJobId(jobId);
        model.addAttribute("applications", applications);
        model.addAttribute("job", job);
        return "employer_applications";
    }

    @GetMapping("/application/{id}/status")
    public String updateApplicationStatus(@PathVariable("id") Long id, @RequestParam("status") String status, @RequestParam("jobId") Long jobId, Authentication authentication) {
        Job job = jobService.findJobById(jobId);
        User employer = resolveUser(authentication);
        if (job != null && employer != null && job.getPostedBy().getId().equals(employer.getId())) {
            applicationService.updateApplicationStatus(id, status);
        }
        return "redirect:/employer/job/" + jobId + "/applications";
    }

    @GetMapping("/applications/{id}/sendEmail")
    public String sendEmail(@PathVariable("id") Long id, @RequestParam("jobId") Long jobId, 
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
                           Authentication authentication) {
        try {
            Job job = jobService.findJobById(jobId);
            User employer = resolveUser(authentication);
            
            if (job == null || employer == null || !job.getPostedBy().getId().equals(employer.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized access.");
                return "redirect:/employer/dashboard";
            }

            employerService.sendEmailConfirmation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Email sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send email: " + e.getMessage());
        }
        return "redirect:/employer/job/" + jobId + "/applications";
    }

    /**
     * Resolves the logged-in user from the Authentication object.
     * Handles both form-login (username field) and OAuth2 (email as username).
     */
    private User resolveUser(Authentication authentication) {
        if (authentication == null) return null;
        String name = authentication.getName();
        User user = userService.findByUsername(name);
        if (user == null) {
            user = userService.findByEmail(name);
        }
        return user;
    }
}
