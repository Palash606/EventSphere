package com.example.eventsphere.controller;

import com.example.eventsphere.model.User;
import com.example.eventsphere.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public")
@AllArgsConstructor
public class PublicController {

    UserService userService;

    /**
     * Displays the registration page for new users.
     * <p>
     * This function handles the GET request for the /register endpoint.
     * It prepares the registration form by adding a new User object to the model.
     *
     * @param model Spring MVC Model object for passing attributes to the view.
     * @return String representing the view name for the registration page.
     */
    @GetMapping("/register")
    public String displayRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register.html";
    }

    /**
     * Creates a new user based on the provided registration information.
     * <p>
     * This function handles the POST request for the /createUser endpoint.
     * It validates the registration form input, creates a new user if validation
     * passes,
     * and redirects to the login page with a success message if the user is
     * successfully created.
     *
     * @param user   User object containing the registration information.
     * @param errors Errors object containing validation errors, if any.
     * @return String representing the view name for redirection based on
     * registration status.
     */
    @PostMapping("/createUser")
    public String createUser(@Valid @ModelAttribute("user") User user, Errors errors) {
        if (errors.hasErrors()) {
            return "register.html";
        }
        boolean isSaved = userService.createUser(user);
        if (isSaved) {
            return "redirect:/login?register=true";
        } else {
            return "register.html";
        }
    }

}