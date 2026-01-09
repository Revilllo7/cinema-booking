package com.cinema.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;
    
    @NotBlank(message = "Password is required", groups = CreateValidation.class)
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Pattern(regexp = "^\\+?[0-9]{9,20}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    private Boolean enabled;
    
    private Set<String> roles;
    
    // Validation group for create operations
    public interface CreateValidation {}
}
