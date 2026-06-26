package com.example.jobportal.controller;

import com.example.jobportal.entity.User;
import com.example.jobportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.jobportal.service.EmailService;
import java.util.Random;
import java.util.UUID;
import java.time.LocalDateTime;

@Controller
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;

    public AuthController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        if (userService.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }
        if (userService.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email already registered!");
            return "register";
        }
        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        user.setEnabled(false);

        System.out.println("DEBUG: OTP for " + user.getEmail() + " is " + otp);
        userService.saveUser(user);

        // Send OTP Email
        try {
            emailService.sendEmail(user.getEmail(), "Your OTP Code - Job Portal", 
                "Welcome to Job Portal! Your OTP for registration is: " + otp + "\nThis code will expire in 5 minutes.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send OTP email. Please ensure your SMTP settings are correct.");
            return "register";
        }

        return "redirect:/verify-otp?email=" + user.getEmail();
    }

    @GetMapping("/verify-otp")
    public String verifyOtpForm(@RequestParam(name = "email", required = false) String email, Model model) {
        if (email == null) {
            return "redirect:/login";
        }
        model.addAttribute("email", email);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("email") String email, @RequestParam("otp") String otp, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "User not found.");
            model.addAttribute("email", email);
            return "verify-otp";
        }

        if (user.isEnabled()) {
            return "redirect:/login?success"; // Already verified
        }

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            model.addAttribute("error", "Invalid OTP code.");
            model.addAttribute("email", email);
            return "verify-otp";
        }

        if (user.getOtpExpiryTime() != null && LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            model.addAttribute("error", "OTP has expired. Please register again.");
            model.addAttribute("email", email);
            return "verify-otp";
        }

        // Verify successful
        user.setEnabled(true);
        user.setOtp(null);
        user.setOtpExpiryTime(null);
        userService.updateUser(user);

        return "redirect:/login?success";
    }

    @GetMapping("/dashboard")
    @SuppressWarnings("null")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "redirect:/no-role";
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");

        if (role.isBlank()) {
            model.addAttribute("errorMessage", "No role assigned to your account. Please contact the administrator.");
            return "redirect:/no-role";
        }

        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (role.equals("ROLE_EMPLOYER")) {
            return "redirect:/employer/dashboard";
        } else {
            return "redirect:/student/dashboard";
        }
    }

    @GetMapping("/no-role")
    public String noRole(Model model) {
        model.addAttribute("statusCode", 403);
        model.addAttribute("errorTitle", "No Role Assigned");
        model.addAttribute("errorMessage",
            "Your account has no role assigned. Please contact the administrator or re-register.");
        return "error";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "No account found with that email address.");
            return "forgot-password";
        }

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
        userService.updateUser(user);

        System.out.println("DEBUG: Password Reset OTP for " + user.getEmail() + " is " + otp);

        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send OTP email. Please ensure your SMTP settings in application.properties are correct.");
            return "forgot-password";
        }

        return "redirect:/verify-reset-otp?email=" + user.getEmail();
    }

    @GetMapping("/verify-reset-otp")
    public String verifyResetOtpForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "verify-reset-otp";
    }

    @PostMapping("/verify-reset-otp")
    public String processVerifyResetOtp(@RequestParam("email") String email, 
                                       @RequestParam("otp") String otp, 
                                       Model model) {
        User user = userService.findByEmail(email);
        if (user == null || user.getOtp() == null || !user.getOtp().equals(otp)) {
            model.addAttribute("error", "Invalid OTP.");
            model.addAttribute("email", email);
            return "verify-reset-otp";
        }

        if (user.getOtpExpiryTime() != null && LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            model.addAttribute("error", "OTP has expired. Please click 'Resend OTP'.");
            model.addAttribute("email", email);
            return "verify-reset-otp";
        }

        // OTP verified
        String verifiedToken = UUID.randomUUID().toString();
        user.setResetToken(verifiedToken);
        userService.updateUser(user);

        return "redirect:/reset-password?token=" + verifiedToken;
    }

    @GetMapping("/resend-reset-otp")
    public String resendResetOtp(@RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/forgot-password?error";
        }

        // Generate new OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
        userService.updateUser(user);

        System.out.println("DEBUG: Resent Password Reset OTP for " + user.getEmail() + " is " + otp);

        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            return "redirect:/verify-reset-otp?email=" + email + "&error=Failed+to+resend+email";
        }

        return "redirect:/verify-reset-otp?email=" + email + "&success=New+OTP+sent";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token, Model model) {
        User user = userService.findByResetToken(token);
        if (user == null) {
            model.addAttribute("error", "Invalid session. Please request a new OTP.");
            return "forgot-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       Model model) {
        User user = userService.findByResetToken(token);
        if (user == null) {
            model.addAttribute("error", "Invalid session.");
            return "forgot-password";
        }

        userService.updatePassword(user, newPassword);
        
        // Send confirmation email
        try {
            emailService.sendPasswordResetConfirmationEmail(user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send reset confirmation email: " + e.getMessage());
        }

        // Clear OTP and token after success
        user.setResetToken(null);
        user.setOtp(null);
        user.setOtpExpiryTime(null);
        userService.updateUser(user);

        // Auto-login after reset
        try {
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    user.getUsername(), null, 
                    org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(user.getRole())
                );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);
            return "redirect:/dashboard";
        } catch (Exception e) {
            return "redirect:/login?resetSuccess";
        }
    }

}
