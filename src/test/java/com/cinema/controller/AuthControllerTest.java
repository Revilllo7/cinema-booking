package com.cinema.controller;

import com.cinema.dto.UserDTO;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	@Mock
	private UserService userService;

	@Mock
	private BindingResult bindingResult;

	@Mock
	private RedirectAttributes redirectAttributes;

	@InjectMocks
	private AuthController authController;

	private Model model;

	@BeforeEach
	void setUp() {
		model = new ExtendedModelMap();
	}

	@Test
	void showLoginPage_WithError_AddsErrorMessage() {
		String view = authController.showLoginPage("true", null, null, model);

		assertThat(view).isEqualTo("login");
		assertThat(model.getAttribute("error")).isEqualTo("Invalid username or password");
	}

	@Test
	void showLoginPage_WithLogoutAndRegistered_AddsMessage() {
		String view = authController.showLoginPage(null, "true", "true", model);

		assertThat(view).isEqualTo("login");
		assertThat(model.getAttribute("message"))
			.isEqualTo("Registration successful! Please log in");
	}

	@Test
	void showRegisterPage_AddsEmptyUser() {
		String view = authController.showRegisterPage(model);

		assertThat(view).isEqualTo("register");
		assertThat(model.getAttribute("userDTO")).isInstanceOf(UserDTO.class);
	}

	@Test
	void handleRegistration_WithValidationErrors_ReturnsRegister() {
		given(bindingResult.hasErrors()).willReturn(true);

		String view = authController.handleRegistration(new UserDTO(), bindingResult, redirectAttributes, model);

		assertThat(view).isEqualTo("register");
		verifyNoInteractions(userService);
	}

	@Test
	void handleRegistration_WhenUsernameExists_ShowsErrorAndStaysOnForm() {
		given(bindingResult.hasErrors()).willReturn(false);
		given(userService.getUserByUsername("existing"))
			.willReturn(UserDTO.builder().username("existing").build());

		UserDTO dto = UserDTO.builder().username("existing").email("new@example.com").build();
		String view = authController.handleRegistration(dto, bindingResult, redirectAttributes, model);

		assertThat(view).isEqualTo("register");
		assertThat(model.getAttribute("error")).isEqualTo("Username already exists");
		verify(userService, never()).createUser(any());
	}

	@Test
	void handleRegistration_WhenEmailExists_ShowsError() {
		given(bindingResult.hasErrors()).willReturn(false);
		willThrow(new ResourceNotFoundException("User not found"))
			.given(userService).getUserByUsername("newuser");
		given(userService.getUserByEmail("duplicate@example.com"))
			.willReturn(UserDTO.builder().email("duplicate@example.com").build());

		UserDTO dto = UserDTO.builder()
			.username("newuser")
			.email("duplicate@example.com")
			.build();

		String view = authController.handleRegistration(dto, bindingResult, redirectAttributes, model);

		assertThat(view).isEqualTo("register");
		assertThat(model.getAttribute("error")).isEqualTo("Email already registered");
		verify(userService, never()).createUser(any());
	}

	@Test
	void handleRegistration_WithValidData_RedirectsToLogin() {
		given(bindingResult.hasErrors()).willReturn(false);
		willThrow(new ResourceNotFoundException("User", "username", "brand-new"))
			.given(userService).getUserByUsername("brand-new");
		willThrow(new ResourceNotFoundException("User", "email", "fresh@example.com"))
			.given(userService).getUserByEmail("fresh@example.com");
		UserDTO dto = UserDTO.builder()
			.username("brand-new")
			.email("fresh@example.com")
			.password("P@ssw0rd1")
			.build();
		given(userService.createUser(dto)).willReturn(dto);

		String view = authController.handleRegistration(dto, bindingResult, redirectAttributes, model);

		assertThat(view).isEqualTo("redirect:/login?registered=true");
		verify(redirectAttributes).addFlashAttribute(eq("message"), contains("Registration successful"));
		verify(userService).createUser(dto);
	}

	@Test
	void handleRegistration_WhenServiceThrows_ReturnsRegisterWithGenericError() {
		given(bindingResult.hasErrors()).willReturn(false);
		willThrow(new ResourceNotFoundException("User", "username", "optimistic"))
			.given(userService).getUserByUsername("optimistic");
		willThrow(new ResourceNotFoundException("User", "email", "optimistic@example.com"))
			.given(userService).getUserByEmail("optimistic@example.com");
		UserDTO dto = UserDTO.builder()
			.username("optimistic")
			.email("optimistic@example.com")
			.password("P@ssword9")
			.build();
		given(userService.createUser(dto)).willThrow(new RuntimeException("boom"));

		String view = authController.handleRegistration(dto, bindingResult, redirectAttributes, model);

		assertThat(view).isEqualTo("register");
		assertThat(model.getAttribute("error")).isEqualTo("Registration failed. Please try again");
		verify(userService).getUserByUsername("optimistic");
		verify(userService).getUserByEmail("optimistic@example.com");
		verify(userService).createUser(dto);
		verifyNoMoreInteractions(userService);
	}
}
