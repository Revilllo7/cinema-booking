package com.cinema.controller.rest;

import com.cinema.dto.UserDTO;
import com.cinema.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management API")
public class UserRestController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Retrieve a paginated list of all users (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "username") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        
        log.info("GET /api/v1/users - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<UserDTO> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a single user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        log.info("GET /api/v1/users/{}", id);
        
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user by username", description = "Retrieve a user by username")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        
        log.info("GET /api/v1/users/username/{}", username);
        
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get current user", description = "Get the currently authenticated user's profile")
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        log.info("GET /api/v1/users/me - User: {}", authentication.getName());
        
        UserDTO user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get users by role", description = "Retrieve all users with a specific role (Admin only)")
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @Parameter(description = "User role name") @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET /api/v1/users/role/{}", role);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> users = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Register new user", description = "Create a new user account (public registration)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("POST /api/v1/users/register - Registering user: {}", userDTO.getUsername());
        
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "Create user", description = "Create a new user (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("POST /api/v1/users - Creating user: {}", userDTO.getUsername());
        
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "Update user", description = "Update user details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        
        log.info("PUT /api/v1/users/{} - Updating user", id);
        
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Enable user", description = "Enable a user account (Admin only)")
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        log.info("PUT /api/v1/users/{}/enable", id);
        
        userService.enableUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Disable user", description = "Disable a user account (Admin only)")
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        log.info("PUT /api/v1/users/{}/disable", id);
        
        userService.disableUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete user", description = "Deactivate a user account (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID") @PathVariable Long id) {
        log.info("DELETE /api/v1/users/{}", id);
        
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
