package com.cinema.controller.web;

import com.cinema.dto.MovieDTO;
import com.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String adminStats(Model model) {
        model.addAttribute("pageTitle", "Sales Statistics");
        return "admin-stats";
    }
}
