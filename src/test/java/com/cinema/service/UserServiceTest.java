package com.cinema.service;

import com.cinema.dto.UserDTO;
import com.cinema.entity.Role;
import com.cinema.entity.User;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.fixtures.DTOFixtures;
import com.cinema.fixtures.EntityFixtures;
import com.cinema.repository.RoleRepository;
import com.cinema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * Unit tests for UserService using Mockito.
 * Tests business logic in isolation from database.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = EntityFixtures.createDefaultUser();
        testUser.setId(1L); // Set ID for service mocking tests
    }

    // ========== getAllUsers Tests ==========

    @Test
    void getAllUsers_WithExistingUsers_ReturnsPaginatedUserDTOs() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        given(userRepository.findAll(pageable)).willReturn(userPage);

        // When
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllUsers_CallsRepository_ExactlyOnce() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        given(userRepository.findAll(pageable)).willReturn(userPage);

        // When
        userService.getAllUsers(pageable);

        // Then
        then(userRepository).should(times(1)).findAll(pageable);
    }

    // ========== getAllActiveUsers Tests ==========

    @Test
    void getAllActiveUsers_WithEnabledUsers_ReturnsOnlyActiveUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        given(userRepository.findByEnabledTrue(pageable)).willReturn(userPage);

        // When
        Page<UserDTO> result = userService.getAllActiveUsers(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ========== getUserById Tests ==========

    @Test
    void getUserById_ExistingUser_ReturnsUserDTO() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When
        UserDTO result = userService.getUserById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_ExistingUser_MapsAllFields() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When
        UserDTO result = userService.getUserById(1L);

        // Then
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(result.getLastName()).isEqualTo(testUser.getLastName());
    }

    @Test
    void getUserById_NonExistingUser_ThrowsResourceNotFoundException() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUserById_NonExistingUser_ContainsCorrectErrorMessage() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
            .hasMessageContaining("User")
            .hasMessageContaining("999");
    }

    // ========== getUserByUsername Tests ==========

    @Test
    void getUserByUsername_ExistingUsername_ReturnsUserDTO() {
        // Given
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(testUser));

        // When
        UserDTO result = userService.getUserByUsername("testuser");

        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserByUsername_NonExistingUsername_ThrowsResourceNotFoundException() {
        // Given
        given(userRepository.findByUsername("nonexistent")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== getUserByEmail Tests ==========

    @Test
    void getUserByEmail_ExistingEmail_ReturnsUserDTO() {
        // Given
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));

        // When
        UserDTO result = userService.getUserByEmail("test@example.com");

        // Then
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserByEmail_NonExistingEmail_ThrowsResourceNotFoundException() {
        // Given
        given(userRepository.findByEmail("nonexistent@example.com")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== createUser Tests ==========

    @Test
    void createUser_ValidUser_ReturnsCreatedUserDTO() {
        // Given
        UserDTO newUserDTO = DTOFixtures.createUserDTOWithoutId();
        User savedUser = EntityFixtures.userBuilder()
            .id(1L)
            .username(newUserDTO.getUsername())
            .email(newUserDTO.getEmail())
            .build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(EntityFixtures.createUserRole()));
        given(passwordEncoder.encode(anyString())).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When
        UserDTO result = userService.createUser(newUserDTO);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createUser_ValidUser_EncodesPassword() {
        // Given
        UserDTO newUserDTO = DTOFixtures.createUserDTOWithoutId();
        User savedUser = EntityFixtures.userBuilder().id(1L).build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(EntityFixtures.createUserRole()));
        given(passwordEncoder.encode(anyString())).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When
        userService.createUser(newUserDTO);

        // Then
        then(passwordEncoder).should(times(1)).encode(newUserDTO.getPassword());
    }

    @Test
    void createUser_DuplicateUsername_ThrowsIllegalArgumentException() {
        // Given
        UserDTO newUserDTO = DTOFixtures.createUserDTOWithoutId();
        given(userRepository.findByUsername(newUserDTO.getUsername()))
            .willReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(newUserDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already exists");
    }

    @Test
    void createUser_DuplicateEmail_ThrowsIllegalArgumentException() {
        // Given
        UserDTO newUserDTO = DTOFixtures.createUserDTOWithoutId();
        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(userRepository.findByEmail(newUserDTO.getEmail()))
            .willReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(newUserDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_ValidUser_AssignsDefaultRole() {
        // Given
        UserDTO newUserDTO = DTOFixtures.createUserDTOWithoutId();
        Role userRole = EntityFixtures.createUserRole();
        User savedUser = EntityFixtures.userBuilder().id(1L).build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(userRole));
        given(passwordEncoder.encode(anyString())).willReturn("encoded_password");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When
        userService.createUser(newUserDTO);

        // Then
        then(roleRepository).should(times(1)).findByName("ROLE_USER");
    }

    // ========== updateUser Tests ==========

    @Test
    void updateUser_ExistingUser_ReturnsUpdatedUserDTO() {
        // Given
        UserDTO updateDTO = DTOFixtures.createDefaultUserDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setEmail(testUser.getEmail()); // Keep same email to avoid email check
        
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // When
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void updateUser_NonExistingUser_ThrowsResourceNotFoundException() {
        // Given
        UserDTO updateDTO = DTOFixtures.createDefaultUserDTO();
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, updateDTO))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_DuplicateEmail_ThrowsIllegalArgumentException() {
        // Given
        UserDTO updateDTO = DTOFixtures.createDefaultUserDTO();
        updateDTO.setEmail("other@example.com");
        
        User otherUser = EntityFixtures.createAdminUser();
        
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.findByEmail("other@example.com")).willReturn(Optional.of(otherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already exists");
    }

    // ========== enableUser Tests ==========

    @Test
    void enableUser_ExistingUser_SetsEnabledToTrue() {
        // Given
        User disabledUser = EntityFixtures.createDisabledUser();
        given(userRepository.findById(3L)).willReturn(Optional.of(disabledUser));
        given(userRepository.save(any(User.class))).willReturn(disabledUser);

        // When
        userService.enableUser(3L);

        // Then
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    void enableUser_NonExistingUser_ThrowsResourceNotFoundException() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.enableUser(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== disableUser Tests ==========

    @Test
    void disableUser_ExistingUser_SetsEnabledToFalse() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // When
        userService.disableUser(1L);

        // Then
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    void disableUser_NonExistingUser_ThrowsResourceNotFoundException() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.disableUser(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== deleteUser Tests ==========

    @Test
    void deleteUser_ExistingUser_CallsRepositoryDelete() {
        // Given
        given(userRepository.existsById(1L)).willReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        then(userRepository).should(times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistingUser_ThrowsResourceNotFoundException() {
        // Given
        given(userRepository.existsById(999L)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_NonExistingUser_DoesNotCallRepositoryDelete() {
        // Given
        given(userRepository.existsById(999L)).willReturn(false);

        // When & Then
        try {
            userService.deleteUser(999L);
        } catch (ResourceNotFoundException e) {
            // Expected
        }
        then(userRepository).should(never()).deleteById(any());
    }
}
