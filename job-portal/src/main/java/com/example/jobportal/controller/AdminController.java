package com.example.jobportal.controller;

import com.example.jobportal.entity.Application;
import com.example.jobportal.entity.User;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.JobService;
import com.example.jobportal.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    public AdminController(UserService userService, JobService jobService, ApplicationService applicationService) {
        this.userService = userService;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> allUsers = userService.findAllUsers();
        List<Application> allApplications = applicationService.findAllApplications();

        long studentCount = allUsers.stream()
                .filter(u -> "ROLE_STUDENT".equals(u.getRole())).count();
        long employerCount = allUsers.stream()
                .filter(u -> "ROLE_EMPLOYER".equals(u.getRole())).count();
        long pendingCount = allApplications.stream()
                .filter(a -> "PENDING".equalsIgnoreCase(a.getStatus())).count();

        // Get the 5 most recent applications
        List<Application> recentApplications = allApplications.stream()
                .sorted((a, b) -> {
                    if (a.getApplicationDate() == null || b.getApplicationDate() == null)
                        return 0;
                    return b.getApplicationDate().compareTo(a.getApplicationDate());
                })
                .limit(5)
                .toList();

        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalJobs", jobService.findAllJobs().size());
        model.addAttribute("totalApplications", allApplications.size());
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("employerCount", employerCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("recentApplications", recentApplications);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        User user = userService.findById(id);
        // Server-side guard: never allow deletion of admin accounts
        if (user != null && !"ROLE_ADMIN".equals(user.getRole())) {
            userService.deleteUser(id);
        }
        return "redirect:/admin/users";
    }
}
