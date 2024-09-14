package com.example.eventsphere.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    /**
     * Displays the login page with optional error, logout, or registration
     * messages.
     * <p>
     * This function handles both GET and POST requests for the /login endpoint.
     * It retrieves optional error, logout, or registration messages from request
     * parameters and
     * prepares the corresponding messages to be displayed on the login page.
     *
     * @param error    Optional parameter indicating login error.
     * @param logout   Optional parameter indicating logout success.
     * @param register Optional parameter indicating successful registration.
     * @param model    Spring MVC Model object for passing attributes to the view.
     * @return String representing the view name for the login page.
     */
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public String displayLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "register", required = false) String register, Model model) {

        String errorMessage = null;
        String infoMessage = null;
        if (error != null) {
            errorMessage = "Username or Password is incorrect.";
        } else if (logout != null) {
            infoMessage = "You have been successfully logged out.";
        } else if (register != null) {
            infoMessage = "You registration successful. Login with registered credentials";
        }

        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("infoMessage", infoMessage);
        return "login.html";
    }

    /**
     * Logs out the authenticated user and redirects to the login page.
     * <p>
     * This function handles the GET request for the /logout endpoint.
     * It logs out the authenticated user by invalidating the current session and
     * clearing the authentication.
     *
     * @param request  HttpServletRequest object.
     * @param response HttpServletResponse object.
     * @return String representing the redirection to the login page with logout
     * success message.
     */
    @GetMapping("/logout")
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout=true";
    }

}
