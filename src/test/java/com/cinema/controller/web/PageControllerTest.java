package com.cinema.controller.web;

import com.cinema.dto.MovieDTO;
import com.cinema.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PageControllerTest {

	@Mock
	private MovieService movieService;

	@InjectMocks
	private PageController pageController;

	private Model model;

	@BeforeEach
	void setUp() {
		model = new ExtendedModelMap();
	}

	@Test
	void movieRedirect_WithId_ReturnsRedirect() {
		String view = pageController.movieRedirect(42L);

		assertThat(view).isEqualTo("redirect:/movie?id=42");
	}

	@Test
	void movie_WithWatchUrl_PopulatesModelAndEmbed() {
		MovieDTO movie = MovieDTO.builder()
			.id(5L)
			.title("Inception")
			.trailerUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
			.build();
		given(movieService.getMovieById(5L)).willReturn(movie);

		String view = pageController.movie(5L, model);

		assertThat(view).isEqualTo("movie");
		assertThat(model.getAttribute("movie")).isEqualTo(movie);
		assertThat(model.getAttribute("trailerEmbedUrl"))
			.isEqualTo("https://www.youtube.com/embed/dQw4w9WgXcQ");
	}

	@Test
	void movie_WithShortYoutubeUrl_EmbedsVideo() {
		MovieDTO movie = MovieDTO.builder()
			.id(7L)
			.title("Dark Knight")
			.trailerUrl("https://youtu.be/abcd1234")
			.build();
		given(movieService.getMovieById(7L)).willReturn(movie);

		String view = pageController.movie(7L, model);

		assertThat(view).isEqualTo("movie");
		assertThat(model.getAttribute("trailerEmbedUrl"))
			.isEqualTo("https://www.youtube.com/embed/abcd1234");
	}

	@Test
	void movie_WhenIdMissing_AddsPageTitleOnly() {
		String view = pageController.movie(null, model);

		assertThat(view).isEqualTo("movie");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Movie Details");
		assertThat(model.containsAttribute("movie")).isFalse();
	}

	@Test
	void movies_ReturnsMoviesViewWithTitle() {
		String view = pageController.movies(model);

		assertThat(view).isEqualTo("movies");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Movies");
	}

	@Test
	void screenings_ReturnsScreeningsViewWithTitle() {
		String view = pageController.screenings(model);

		assertThat(view).isEqualTo("screenings");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Screenings");
	}

	@Test
	void seatSelection_ReturnsSeatSelectionView() {
		String view = pageController.seatSelection(model);

		assertThat(view).isEqualTo("seat-selection");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Seat Selection");
	}

	@Test
	void cart_ReturnsCartView() {
		String view = pageController.cart(model);

		assertThat(view).isEqualTo("cart");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Your Cart");
	}

	@Test
	void checkout_ReturnsCheckoutView() {
		String view = pageController.checkout(model);

		assertThat(view).isEqualTo("checkout");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Checkout");
	}

	@Test
	void adminStats_ReturnsAdminStatsView() {
		String view = pageController.adminStats(model);

		assertThat(view).isEqualTo("admin-stats");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Sales Statistics");
	}

	@Test
	void adminDashboard_ReturnsAdminDashboard() {
		String view = pageController.adminDashboard(model);

		assertThat(view).isEqualTo("admin-dashboard");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Admin Dashboard");
	}

	@Test
	void adminBookings_ReturnsAdminBookings() {
		String view = pageController.adminBookings(model);

		assertThat(view).isEqualTo("admin-bookings");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Manage Bookings");
	}

	@Test
	void adminMovies_ReturnsAdminMovies() {
		String view = pageController.adminMovies(model);

		assertThat(view).isEqualTo("admin-movies");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Manage Movies");
	}

	@Test
	void adminScreenings_ReturnsAdminScreenings() {
		String view = pageController.adminScreenings(model);

		assertThat(view).isEqualTo("admin-screenings");
		assertThat(model.getAttribute("pageTitle")).isEqualTo("Manage Screenings");
	}

	@Test
	void bookings_ReturnsProfileRedirect() {
		String view = pageController.bookings();

		assertThat(view).isEqualTo("redirect:/profile/bookings");
	}
}
