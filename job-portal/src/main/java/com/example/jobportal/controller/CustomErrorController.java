package com.example.jobportal.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int statusCode = 500;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }

        model.addAttribute("statusCode", statusCode);

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("errorTitle", "Page Not Found");
            model.addAttribute("errorMessage", "The page you are looking for does not exist.");
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            model.addAttribute("errorTitle", "Access Denied");
            model.addAttribute("errorMessage", "You do not have permission to access this page.");
        } else {
            model.addAttribute("errorTitle", "Something Went Wrong");
            String msg = (message != null && !message.toString().isBlank())
                    ? message.toString()
                    : "An unexpected error occurred. Please try again.";
            model.addAttribute("errorMessage", msg);
        }

        return "error";
    }
}
