package com.cinema.controller.web;

import com.cinema.dto.MovieDTO;
import com.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final MovieService movieService;

    @GetMapping("/movies/{id}")
    public String movieRedirect(@PathVariable("id") Long id) {
        return "redirect:/movie?id=" + id;
    }

    @GetMapping("/movie")
    public String movie(@RequestParam(value = "id", required = false) Long id, Model model) {
        model.addAttribute("pageTitle", "Movie Details");
        if (id != null) {
            MovieDTO movie = movieService.getMovieById(id);
            model.addAttribute("movie", movie);
            model.addAttribute("trailerEmbedUrl", buildTrailerEmbedUrl(movie));
        }
        return "movie";
    }

    @GetMapping("/movies")
    public String movies(Model model) {
        model.addAttribute("pageTitle", "Movies");
        return "movies";
    }

    @GetMapping("/screenings")
    public String screenings(Model model) {
        model.addAttribute("pageTitle", "Screenings");
        return "screenings";
    }

    @GetMapping("/seat-selection")
    public String seatSelection(Model model) {
        model.addAttribute("pageTitle", "Seat Selection");
        return "seat-selection";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("pageTitle", "Your Cart");
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("pageTitle", "Checkout");
        return "checkout";
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminStats(Model model) {
        model.addAttribute("pageTitle", "Sales Statistics");
        return "admin-stats";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin-dashboard";
    }

    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminBookings(Model model) {
        model.addAttribute("pageTitle", "Manage Bookings");
        return "admin-bookings";
    }

    @GetMapping("/admin/movies")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminMovies(Model model) {
        model.addAttribute("pageTitle", "Manage Movies");
        return "admin-movies";
    }

    @GetMapping("/admin/screenings")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminScreenings(Model model) {
        model.addAttribute("pageTitle", "Manage Screenings");
        return "admin-screenings";
    }

    @GetMapping("/bookings")
    public String bookings() {
        return "redirect:/profile/bookings";
    }

    private String buildTrailerEmbedUrl(MovieDTO movie) {
        if (movie == null || !StringUtils.hasText(movie.getTrailerUrl())) {
            return null;
        }
        String url = movie.getTrailerUrl().trim();
        if (url.contains("youtube.com/watch")) {
            String videoId = extractQueryParam(url, "v");
            if (StringUtils.hasText(videoId)) {
                return "https://www.youtube.com/embed/" + videoId;
            }
        } else if (url.contains("youtu.be/")) {
            String videoId = url.substring(url.lastIndexOf('/') + 1);
            return "https://www.youtube.com/embed/" + videoId;
        } else if (url.contains("youtube.com/embed/")) {
            return url;
        }
        return url;
    }

    private String extractQueryParam(String url, String param) {
        int start = url.indexOf('?');
        if (start == -1) {
            return null;
        }
        String query = url.substring(start + 1);
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && param.equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
