package com.cinema.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SecurityConfig.
 * Tests bean creation and password encoding.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should create SecurityFilterChain bean")
    void securityFilterChainBeanExists() {
        SecurityFilterChain bean = applicationContext.getBean(SecurityFilterChain.class);
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Should create PasswordEncoder bean")
    void passwordEncoderBeanExists() {
        PasswordEncoder bean = applicationContext.getBean(PasswordEncoder.class);
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("PasswordEncoder should be BCryptPasswordEncoder")
    void passwordEncoderIsBCrypt() {
        PasswordEncoder encoder = applicationContext.getBean(PasswordEncoder.class);
        String rawPassword = "TestPassword123!";
        String encoded = encoder.encode(rawPassword);

        assertThat(encoded).matches("\\$2[aby]\\$\\d{2}\\$.*");
        assertThat(encoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("Should create AuthenticationManager bean")
    void authenticationManagerBeanExists() {
        org.springframework.security.authentication.AuthenticationManager bean =
            applicationContext.getBean(org.springframework.security.authentication.AuthenticationManager.class);
        assertThat(bean).isNotNull();
    }

    @Test
    @DisplayName("Password encoder should encode passwords")
    void passwordEncoder_EncodesPassword() {
        PasswordEncoder encoder = applicationContext.getBean(PasswordEncoder.class);
        String rawPassword = "MySecurePassword123!";

        String encoded = encoder.encode(rawPassword);

        assertThat(encoded).isNotBlank();
        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(encoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("Password encoder should not match wrong password")
    void passwordEncoder_DoesNotMatchWrongPassword() {
        PasswordEncoder encoder = applicationContext.getBean(PasswordEncoder.class);
        String rawPassword = "MySecurePassword123!";
        String wrongPassword = "DifferentPassword123!";

        String encoded = encoder.encode(rawPassword);

        assertThat(encoder.matches(wrongPassword, encoded)).isFalse();
    }

    @Test
    @DisplayName("Password encoder should produce different hash each time")
    void passwordEncoder_ProducedDifferentHashEachTime() {
        PasswordEncoder encoder = applicationContext.getBean(PasswordEncoder.class);
        String rawPassword = "MySecurePassword123!";

        String encoded1 = encoder.encode(rawPassword);
        String encoded2 = encoder.encode(rawPassword);

        assertThat(encoded1).isNotEqualTo(encoded2);
        assertThat(encoder.matches(rawPassword, encoded1)).isTrue();
        assertThat(encoder.matches(rawPassword, encoded2)).isTrue();
    }

    @Test
    @DisplayName("SecurityConfig should enable method security")
    void methodSecurityEnabled() {
        assertThat(applicationContext.getBeansWithAnnotation(
            org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class))
            .isNotEmpty();
    }
}
