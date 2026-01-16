package com.cinema.controller;

import com.cinema.dto.BookingDTO;
import com.cinema.dto.UserDTO;
import com.cinema.service.BookingService;
import com.cinema.service.UserService;
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
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerTest {

	@Mock
	private UserService userService;

	@Mock
	private BookingService bookingService;

	@InjectMocks
	private UserProfileController userProfileController;

	private Principal principal;
	private Model model;
	private UserDTO user;

	@BeforeEach
	void setUp() {
		principal = () -> "jane";
		model = new ExtendedModelMap();
		user = UserDTO.builder().id(1L).username("jane").build();
	}

	@Test
	void showProfile_AddsUserFromPrincipal() {
		given(userService.getUserByUsername("jane")).willReturn(user);

		String view = userProfileController.showProfile(principal, model);

		assertThat(view).isEqualTo("profile");
		assertThat(model.getAttribute("user")).isEqualTo(user);
	}

	@Test
	void showBookingHistory_AddsBookingsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		BookingDTO booking = BookingDTO.builder().id(100L).build();
		Page<BookingDTO> bookingsPage = new PageImpl<>(List.of(booking), pageable, 1);
		given(userService.getUserByUsername("jane")).willReturn(user);
		given(bookingService.getBookingsByUser(eq(1L), eq(pageable))).willReturn(bookingsPage);

		String view = userProfileController.showBookingHistory(principal, pageable, model);

		assertThat(view).isEqualTo("bookings");
		assertThat(model.getAttribute("user")).isEqualTo(user);
		assertThat(model.getAttribute("bookings")).isEqualTo(bookingsPage);
	}

	@Test
	void showPasswordChangePage_ReturnsRedirectAndMessage() {
		String view = userProfileController.showPasswordChangePage(model);

		assertThat(view).isEqualTo("redirect:/profile?todo=password");
		assertThat(model.getAttribute("message"))
			.isEqualTo("TODO: Password change functionality not yet implemented");
	}
}
