package com.example.jobportal.controller;

import com.example.jobportal.entity.Application;
import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.User;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.JobService;
import com.example.jobportal.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final JobService jobService;
    private final UserService userService;
    private final ApplicationService applicationService;

    @Value("${upload.dir}")
    private String uploadDir;

    public StudentController(JobService jobService, UserService userService, ApplicationService applicationService) {
        this.jobService = jobService;
        this.userService = userService;
        this.applicationService = applicationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User student = resolveUser(authentication);
        if (student == null) {
            return "redirect:/no-role";
        }
        int applicationCount = applicationService.getApplicationsByApplicantId(student.getId()).size();
        int availableJobs = jobService.findAllJobs().size();
        model.addAttribute("student", student);
        model.addAttribute("applicationCount", applicationCount);
        model.addAttribute("availableJobs", availableJobs);
        return "student/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        User student = resolveUser(authentication);
        if (student == null) return "redirect:/no-role";
        model.addAttribute("student", student);
        return "student/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("student") User updatedStudent,
                                @RequestParam("resume") MultipartFile multipartFile,
                                Authentication authentication) {
        User student = resolveUser(authentication);
        if (student == null) return "redirect:/no-role";
        student.setSkills(updatedStudent.getSkills());
        student.setEducation(updatedStudent.getEducation());

        if (!multipartFile.isEmpty()) {
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                return "redirect:/student/profile?error=invalid_file";
            }
            String fileName = StringUtils.cleanPath(originalFilename);
            try {
                student.setResumeUrl(fileName);
                String uploadDirLocal = uploadDir + "/" + student.getId();
                Path uploadPath = Paths.get(uploadDirLocal);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                try (java.io.InputStream inputStream = multipartFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ioe) {
                return "redirect:/student/profile?error=upload_failed";
            }
        }
        userService.updateUser(student);
        return "redirect:/student/profile?success";
    }

    @PostMapping("/job/apply/{jobId}")
    public String applyJob(@PathVariable("jobId") Long jobId, Authentication authentication) {
        User student = resolveUser(authentication);
        if (student == null) return "redirect:/no-role";
        if (applicationService.hasAlreadyApplied(student.getId(), jobId)) {
            return "redirect:/jobs?error=already_applied";
        }
        Job job = jobService.findJobById(jobId);
        if (job == null) {
            return "redirect:/jobs?error=job_not_found";
        }
        Application application = new Application();
        application.setApplicant(student);
        application.setJob(job);
        application.setStatus("PENDING");
        applicationService.applyForJob(application);
        return "redirect:/student/applications?success";
    }

    @GetMapping("/applications")
    public String myApplications(Model model, Authentication authentication) {
        User student = resolveUser(authentication);
        if (student == null) return "redirect:/no-role";
        List<Application> applications = applicationService.getApplicationsByApplicantId(student.getId());
        model.addAttribute("applications", applications);
        return "student/applications";
    }

    /**
     * Resolves the logged-in user from the Authentication object.
     * Handles both form-login users (username = username field) and
     * OAuth2 users (username = email, since we store email as username).
     */
    private User resolveUser(Authentication authentication) {
        if (authentication == null) return null;
        String name = authentication.getName();
        User user = userService.findByUsername(name);
        if (user == null) {
            // OAuth2 fallback: name may be the email
            user = userService.findByEmail(name);
        }
        return user;
    }
}
