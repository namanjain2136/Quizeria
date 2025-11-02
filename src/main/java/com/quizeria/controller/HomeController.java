package com.quizeria.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        model.addAttribute("title", "Welcome to Quizeria!");
        
        // Redirect authenticated users to appropriate dashboards
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                return "redirect:/admin";
            } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
                return "redirect:/user";
            }
        }
        
        return "home";
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        // Redirect if already authenticated
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                return "redirect:/admin";
            } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
                return "redirect:/user";
            }
        }
        return "login";
    }
}
