package com.cinema.controller;

import com.cinema.dto.UserDTO;
import com.cinema.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling authentication-related pages (login and registration).
 * Following layer structure: Controller ↔ DTO ↔ Service ↔ Entity ↔ Database
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final UserService userService;
    
    /**
     * Display login page.
     * Configured in SecurityConfig to handle authentication at /perform_login
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {
        
        log.debug("Displaying login page. Error: {}, Logout: {}, Registered: {}", 
                  error, logout, registered);
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please log in");
        }
        
        return "login";
    }
    
    /**
     * Display registration page.
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        log.debug("Displaying registration page");
        model.addAttribute("userDTO", new UserDTO());
        return "register";
    }
    
    /**
     * Handle user registration.
     * Creates new user with ROLE_USER and redirects to login page.
     */
    @PostMapping("/register")
    public String handleRegistration(
            @Valid @ModelAttribute("userDTO") UserDTO userDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        log.info("Processing registration for username: {}", userDTO.getUsername());
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Registration validation failed for username: {}", userDTO.getUsername());
            return "register";
        }
        
        try {
            // Check if username already exists
            try {
                userService.getUserByUsername(userDTO.getUsername());
                log.warn("Registration failed - username already exists: {}", userDTO.getUsername());
                model.addAttribute("error", "Username already exists");
                return "register";
            } catch (Exception e) {
                // Username doesn't exist - this is good
            }
            
            // Check if email already exists
            try {
                userService.getUserByEmail(userDTO.getEmail());
                log.warn("Registration failed - email already exists: {}", userDTO.getEmail());
                model.addAttribute("error", "Email already registered");
                return "register";
            } catch (Exception e) {
                // Email doesn't exist - this is good
            }
            
            // Create user with default ROLE_USER
            UserDTO createdUser = userService.createUser(userDTO);
            log.info("User registered successfully: {}", createdUser.getUsername());
            
            // TODO: Email Verification
            // To implement email verification:
            // 1. Generate verification token (UUID) and store in User entity or separate VerificationToken entity
            // 2. Set user.enabled = false initially
            // 3. Send email with verification link: /verify-email?token={token}
            // 4. Create endpoint to handle verification: verify token, set user.enabled = true
            // 5. Add EmailService with JavaMailSender for sending emails
            // 6. Configure mail properties in application.yml (smtp host, port, username, password)
            
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please log in");
            return "redirect:/login?registered=true";
            
        } catch (Exception e) {
            log.error("Error during registration for username: {}", userDTO.getUsername(), e);
            model.addAttribute("error", "Registration failed. Please try again");
            return "register";
        }
    }
}
