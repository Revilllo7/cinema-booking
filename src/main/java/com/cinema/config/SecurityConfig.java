package com.cinema.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Static resources and authentication pages
                .requestMatchers("/", "/home", "/about", "/contact", "/css/**", "/js/**", "/images/**", "/login", "/register").permitAll()
                // Public booking lookups (id and number)
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/bookings/number/**").permitAll()
                // Booking endpoints must be authenticated for other operations
                .requestMatchers("/api/v1/bookings/**", "/bookings/**").authenticated()
                // Booking endpoints must be authenticated first so anonymous users get 401
                // Public API Endpoints (GET only)
                .requestMatchers(HttpMethod.GET, "/api/v1/movies/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/screenings/**").permitAll()
                // Swagger/OpenAPI restricted to admins (must be above catch-all GET rule)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").hasRole("ADMIN")
                // Allow other GET requests for public access (will 404 if not found, not 401)
                .requestMatchers(HttpMethod.GET, "/**").permitAll()
                // Public registration
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll()
                // H2 Console
                .requestMatchers("/h2-console/**").permitAll()
                // Profile endpoints require authentication
                .requestMatchers("/profile/**").hasAnyRole("USER", "ADMIN")
                // Admin endpoints
                .requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    {
                        // Anonymous users should receive 401 instead of 403
                        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        } else {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        }
                    })
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecretKey")
                .tokenValiditySeconds(86400 * 7) // 7 days
                .rememberMeParameter("remember-me")
                .rememberMeCookieName("cinema-remember-me")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "cinema-remember-me")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/api/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
