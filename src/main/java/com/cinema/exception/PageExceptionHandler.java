package com.cinema.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PageExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException ex,
                                               HttpServletRequest request,
                                               HttpServletResponse response) throws ResourceNotFoundException {
        String path = request.getRequestURI();
        if (isApiRequest(request)) {
            throw ex;
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
