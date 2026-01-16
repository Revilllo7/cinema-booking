package com.cinema.service;

import com.cinema.entity.Role;
import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ReturnsUserDetails() {
        Role adminRole = Role.builder().name("ROLE_ADMIN").build();
        User user = User.builder()
            .id(1L)
            .username("jane")
            .password("encoded")
            .enabled(true)
            .roles(Set.of(adminRole))
            .build();

        when(userRepository.findActiveUserByUsername("jane")).thenReturn(java.util.Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("jane");

        assertThat(details.getUsername()).isEqualTo("jane");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_WhenUserDisabled_MarksAccountLocked() {
        User user = User.builder()
            .id(2L)
            .username("john")
            .password("encoded")
            .enabled(false)
            .roles(Set.of(Role.builder().name("ROLE_USER").build()))
            .build();

        when(userRepository.findActiveUserByUsername("john")).thenReturn(java.util.Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("john");

        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_WhenUserMissing_ThrowsException() {
        when(userRepository.findActiveUserByUsername("ghost")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("ghost"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("ghost");
    }
}
