package com.example.eventsphere.controller;

import com.example.eventsphere.model.User;
import com.example.eventsphere.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    UserRepository userRepository;

    public DashboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Displays the dashboard page for the authenticated user.
     *
     * @param model          Spring MVC Model object.
     * @param authentication Authentication object providing user authentication
     *                       details.
     * @param session        HttpSession object for storing user information.
     * @return String indicating the view name for the dashboard page.
     */
    @GetMapping("/dashboard")
    public String displayDashboard(Model model, Authentication authentication, HttpSession session) {

        User user = userRepository.readByEmail(authentication.getName());

        model.addAttribute("username", user.getUsername());
        model.addAttribute("roles", authentication.getAuthorities().toString());

        session.setAttribute("loggedInUser", user);

        return "dashboard";
    }

}