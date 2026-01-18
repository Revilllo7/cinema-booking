package com.cinema.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.cinema.controller.AuthController;
import com.cinema.controller.UserProfileController;
import com.cinema.controller.web.HomeController;
import com.cinema.controller.web.PageController;

@ControllerAdvice(assignableTypes = {
    HomeController.class,
    PageController.class,
    AuthController.class,
    UserProfileController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PageExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException ex,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        String path = request.getRequestURI();
        if (isApiRequest(request)) {
            return null;
        }
        response.setStatus(HttpStatus.NOT_FOUND.value());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("message", ex.getMessage());
        modelAndView.addObject("path", path);
        return modelAndView;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api")) {
            return true;
        }
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("application/json");
    }
}
