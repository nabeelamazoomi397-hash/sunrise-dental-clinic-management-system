package com.sunrisedental.clinicmanagement.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            Authentication authentication,
            Model model) {

        model.addAttribute(
                "username",
                authentication.getName());

        model.addAttribute(
                "authorities",
                authentication.getAuthorities());

        return "dashboard/index";
    }

    @GetMapping("/access-denied")
    public String showAccessDeniedPage() {
        return "error/access-denied";
    }
}