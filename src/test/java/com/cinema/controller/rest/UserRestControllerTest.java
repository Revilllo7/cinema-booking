package com.cinema.controller.rest;

import com.cinema.dto.UserDTO;
import com.cinema.exception.DuplicateResourceException;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.fixtures.ControllerTestFixtures;
import com.cinema.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserRestController.
 * Tests user endpoints with authorization and error handling.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("UserRestController Tests")
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDTO validUserDTO;

    @BeforeEach
    void setUp() {
        validUserDTO = ControllerTestFixtures.createValidUserDTOForRegistration();
    }

    @Nested
    @DisplayName("GET /api/v1/users")
    class GetAllUsers {

        @Test
        @DisplayName("Admin can view all users")
        @WithMockUser(roles = "ADMIN")
        void getAllUsers_AsAdmin_ReturnsAll() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(userService).getAllUsers(any());
        }

        @Test
        @DisplayName("User cannot view all users")
        @WithMockUser(roles = "USER")
        void getAllUsers_AsUser_Forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/users")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(userService, never()).getAllUsers(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("Admin can get user by ID")
        @WithMockUser(roles = "ADMIN")
        void getUserById_AsAdmin_Success() throws Exception {
            UserDTO user = validUserDTO;
            user.setId(1L);
            when(userService.getUserById(1L)).thenReturn(user);

            mockMvc.perform(get("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(userService).getUserById(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        @WithMockUser(roles = "ADMIN")
        void getUserById_NotFound() throws Exception {
            when(userService.getUserById(999L))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            mockMvc.perform(get("/api/v1/users/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(userService).getUserById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/register")
    class RegisterUser {

        @Test
        @DisplayName("Anonymous can register new user")
        void registerUser_Success() throws Exception {
            UserDTO registered = validUserDTO;
            registered.setId(1L);
            when(userService.createUser(any(UserDTO.class))).thenReturn(registered);

            mockMvc.perform(post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validUserDTO)))
                    .andExpect(status().isCreated());

            verify(userService).createUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should reject duplicate username")
        void registerUser_DuplicateUsername() throws Exception {
            when(userService.createUser(any(UserDTO.class)))
                    .thenThrow(new DuplicateResourceException("Username already exists"));

            mockMvc.perform(post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validUserDTO)))
                    .andExpect(status().isConflict());

            verify(userService).createUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should reject invalid email")
        void registerUser_InvalidEmail() throws Exception {
            UserDTO invalidUser = validUserDTO;
            invalidUser.setEmail("invalid-email");

            mockMvc.perform(post("/api/v1/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(invalidUser)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any(UserDTO.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUserAsAdmin {

        @Test
        @DisplayName("Admin can create user")
        @WithMockUser(roles = "ADMIN")
        void createUser_AsAdmin_Success() throws Exception {
            UserDTO created = validUserDTO;
            created.setId(1L);
            when(userService.createUser(any(UserDTO.class))).thenReturn(created);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validUserDTO)))
                    .andExpect(status().isCreated());

            verify(userService).createUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("User cannot create other users")
        @WithMockUser(roles = "USER")
        void createUser_AsUser_Forbidden() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validUserDTO)))
                    .andExpect(status().isForbidden());

            verify(userService, never()).createUser(any(UserDTO.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("Admin can update user")
        @WithMockUser(roles = "ADMIN")
        void updateUser_AsAdmin_Success() throws Exception {
            UserDTO updated = validUserDTO;
            updated.setId(1L);
            when(userService.updateUser(eq(1L), any(UserDTO.class)))
                    .thenReturn(updated);

            mockMvc.perform(put("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(updated)))
                    .andExpect(status().isOk());

            verify(userService).updateUser(eq(1L), any(UserDTO.class));
        }

        @Test
        @DisplayName("User cannot update other users")
        @WithMockUser(roles = "USER")
        void updateUser_AsUser_Forbidden() throws Exception {
            mockMvc.perform(put("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validUserDTO)))
                    .andExpect(status().isForbidden());

            verify(userService, never()).updateUser(anyLong(), any(UserDTO.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        @WithMockUser(roles = "ADMIN")
        void updateUser_NotFound() throws Exception {
            when(userService.updateUser(eq(999L), any(UserDTO.class)))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            mockMvc.perform(put("/api/v1/users/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validUserDTO)))
                    .andExpect(status().isNotFound());

            verify(userService).updateUser(eq(999L), any(UserDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("Admin can delete user")
        @WithMockUser(roles = "ADMIN")
        void deleteUser_AsAdmin_Success() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(1L);
        }

        @Test
        @DisplayName("User cannot delete other users")
        @WithMockUser(roles = "USER")
        void deleteUser_AsUser_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(userService, never()).deleteUser(anyLong());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        @WithMockUser(roles = "ADMIN")
        void deleteUser_NotFound() throws Exception {
            doThrow(new ResourceNotFoundException("User not found"))
                    .when(userService).deleteUser(999L);

            mockMvc.perform(delete("/api/v1/users/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(userService).deleteUser(999L);
        }
    }
}
