package com.cinema.service;

import com.cinema.entity.Role;
import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's UserDetailsService for loading user authentication details.
 * 
 * Retrieves user information from the database during the authentication process.
 * Converts the domain User entity into a Spring Security UserDetails object with appropriate authorities.
 * 
**/

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user for authentication: {}", username);
        User user = userRepository.findActiveUserByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found or disabled: " + username));

        Collection<GrantedAuthority> authorities = user.getRoles().stream()
            .map(Role::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .accountLocked(!user.getEnabled())
            .disabled(!user.getEnabled())
            .build();
    }
}
