package com.example.jobportal.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;

@Service
public class EmailService {

    private final Optional<JavaMailSender> mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(Optional<JavaMailSender> mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(String to, String subject, String body) {
        if (mailSender.isPresent()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.get().send(message);
                System.out.println("Plain text Email sent successfully to: " + to);
            } catch (Exception e) {
                System.err.println("Failed to send plain text email to " + to + ": " + e.getMessage());
            }
        } else {
            System.out.println("Mail server not configured. Logging plain text email:");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
        }
    }

    @SuppressWarnings("null")
    public void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        if (mailSender.isPresent()) {
            try {
                MimeMessage mimeMessage = mailSender.get().createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                String htmlContent = templateEngine.process(templateName, context);

                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.get().send(mimeMessage);
                System.out.println("HTML Email sent successfully to: " + to);
            } catch (MessagingException e) {
                System.err.println("Failed to send HTML email to " + to + ": " + e.getMessage());
                throw new RuntimeException("Email sending failed", e);
            }
        } else {
            System.out.println("Mail server not configured. Logging email details:");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Template: " + templateName);
        }
    }

    public void sendJobStatusEmail(String to, String candidateName, com.example.jobportal.entity.Job job, String status) {
        Context context = new Context();
        context.setVariable("candidateName", candidateName);
        context.setVariable("jobTitle", job.getTitle());
        context.setVariable("category", job.getCategory());
        context.setVariable("location", job.getLocation());
        context.setVariable("experience", job.getExperience());
        context.setVariable("salary", job.getSalary());
        context.setVariable("skills", job.getSkills());
        context.setVariable("description", job.getDescription());
        context.setVariable("companyName", "Job Portal");

        String subject = "Application Update - Job Portal";
        String templateName;

        switch (status.toUpperCase()) {
            case "SHORTLISTED":
                templateName = "emails/shortlisted";
                break;
            case "REJECTED":
                templateName = "emails/rejected";
                break;
            case "PENDING":
                templateName = "emails/pending";
                break;
            default:
                throw new IllegalArgumentException("Invalid status: " + status);
        }

        sendHtmlEmail(to, subject, templateName, context);
    }

    // Legacy methods updated to be safe (though ideally called with Job object)
    public void sendShortlistNotification(String to, String studentName, String jobTitle, String employerName) {
        // This is a fallback; ideally use the new Job-based method
        com.example.jobportal.entity.Job dummyJob = new com.example.jobportal.entity.Job();
        dummyJob.setTitle(jobTitle);
        dummyJob.setCategory("N/A");
        dummyJob.setLocation("N/A");
        dummyJob.setExperience("N/A");
        dummyJob.setSalary("N/A");
        dummyJob.setSkills("N/A");
        dummyJob.setDescription("N/A");
        sendJobStatusEmail(to, studentName, dummyJob, "SHORTLISTED");
    }

    public void sendRejectionNotification(String to, String studentName, String jobTitle, String employerName) {
        com.example.jobportal.entity.Job dummyJob = new com.example.jobportal.entity.Job();
        dummyJob.setTitle(jobTitle);
        dummyJob.setCategory("N/A");
        dummyJob.setLocation("N/A");
        dummyJob.setExperience("N/A");
        dummyJob.setSalary("N/A");
        dummyJob.setSkills("N/A");
        dummyJob.setDescription("N/A");
        sendJobStatusEmail(to, studentName, dummyJob, "REJECTED");
    }

    public void sendOtpEmail(String to, String otp) {
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("companyName", "Job Portal");
        
        String subject = "Password Reset OTP - Job Portal";
        sendHtmlEmail(to, subject, "emails/otp", context);
    }

    public void sendPasswordResetConfirmationEmail(String to) {
        Context context = new Context();
        context.setVariable("companyName", "Job Portal");
        
        String subject = "Password Reset Successful - Job Portal";
        sendHtmlEmail(to, subject, "emails/password_reset_success", context);
    }
}
