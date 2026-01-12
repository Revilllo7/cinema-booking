package com.cinema.controller;

import com.cinema.dto.BookingDTO;
import com.cinema.dto.UserDTO;
import com.cinema.service.BookingService;
import com.cinema.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller for user profile and booking history pages.
 * Requires authentication to access all endpoints.
 * Following layer structure: Controller ↔ DTO ↔ Service ↔ Entity ↔ Database
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class UserProfileController {
    
    private final UserService userService;
    private final BookingService bookingService;
    
    /**
     * Display user profile page with personal information.
     */
    @GetMapping
    public String showProfile(Principal principal, Model model) {
        String username = principal.getName();
        log.debug("Displaying profile for user: {}", username);
        
        UserDTO user = userService.getUserByUsername(username);
        
        model.addAttribute("user", user);
        return "profile";
    }
    
    /**
     * Display user's booking history with pagination.
     */
    @GetMapping("/bookings")
    public String showBookingHistory(
            Principal principal,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            Model model) {
        
        String username = principal.getName();
        log.debug("Displaying booking history for user: {}, page: {}", username, pageable.getPageNumber());
        
        UserDTO user = userService.getUserByUsername(username);
        
        Page<BookingDTO> bookings = bookingService.getBookingsByUser(user.getId(), pageable);
        
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        return "bookings";
    }
    
    /**
     * TODO: Password Reset Endpoint
     * Implement password change functionality:
     * 1. Create PasswordChangeDTO with currentPassword, newPassword, confirmNewPassword fields
     * 2. Add @PostMapping("/change-password") endpoint
     * 3. Verify current password using passwordEncoder.matches()
     * 4. Validate new password strength (min 8 chars, contains uppercase, lowercase, digit, special char)
     * 5. Check newPassword matches confirmNewPassword
     * 6. Update password using userService.updatePassword(userId, encodedNewPassword)
     * 7. Return success/error message to profile page
     * 8. Optionally: Invalidate all sessions and require re-login for security
     * 9. Optionally: Send email notification about password change
     */
    
    /**
     * Display password change form (placeholder).
     */
    @GetMapping("/change-password")
    public String showPasswordChangePage(Model model) {
        log.debug("Password change page requested");
        model.addAttribute("message", "TODO: Password change functionality not yet implemented");
        return "redirect:/profile?todo=password";
    }
}
