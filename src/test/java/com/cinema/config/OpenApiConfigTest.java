package com.cinema.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for OpenApiConfig.
 * Tests OpenAPI bean configuration and API documentation setup.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("OpenApiConfig Tests")
class OpenApiConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    private OpenAPI openAPI;

    @BeforeEach
    void setUp() {
        openAPI = applicationContext.getBean("cinemaBookingOpenAPI", OpenAPI.class);
    }

    @Test
    @DisplayName("Should create OpenAPI bean with name cinemaBookingOpenAPI")
    void openAPIBeanExists() {
        assertThat(openAPI).isNotNull();
    }

    @Test
    @DisplayName("OpenAPI bean should be of correct type")
    void openAPIBeanIsCorrectType() {
        assertThat(openAPI).isInstanceOf(OpenAPI.class);
    }

    @Test
    @DisplayName("OpenAPI bean should be singleton")
    void openAPIBeanIsSingleton() {
        OpenAPI openAPI2 = applicationContext.getBean("cinemaBookingOpenAPI", OpenAPI.class);
        assertThat(openAPI).isSameAs(openAPI2);
    }

    @Test
    @DisplayName("Should have API info configured")
    void apiInfo_IsConfigured() {
        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
    }

    @Test
    @DisplayName("Should have correct API title")
    void apiTitle_IsCorrect() {
        Info info = openAPI.getInfo();
        assertThat(info.getTitle()).isEqualTo("Cinema Booking System API");
    }

    @Test
    @DisplayName("Should have correct API version")
    void apiVersion_IsCorrect() {
        Info info = openAPI.getInfo();
        assertThat(info.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Should have API description")
    void apiDescription_Exists() {
        Info info = openAPI.getInfo();
        assertThat(info.getDescription()).isNotBlank();
        assertThat(info.getDescription())
            .contains("cinema booking", "API");
    }

    @Test
    @DisplayName("Should have contact information configured")
    void contact_IsConfigured() {
        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact).isNotNull();
    }

    @Test
    @DisplayName("Should have contact name")
    void contactName_IsSet() {
        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact.getName()).isNotBlank();
        assertThat(contact.getName()).containsIgnoringCase("Cinema");
    }

    @Test
    @DisplayName("Should have contact email")
    void contactEmail_IsSet() {
        Contact contact = openAPI.getInfo().getContact();
        assertThat(contact.getEmail()).isNotBlank();
        assertThat(contact.getEmail()).contains("@");
    }

    @Test
    @DisplayName("Should have license information configured")
    void license_IsConfigured() {
        License license = openAPI.getInfo().getLicense();
        assertThat(license).isNotNull();
    }

    @Test
    @DisplayName("Should have license name")
    void licenseName_IsSet() {
        License license = openAPI.getInfo().getLicense();
        assertThat(license.getName()).isNotBlank();
        assertThat(license.getName()).contains("MIT");
    }

    @Test
    @DisplayName("Should have license URL")
    void licenseUrl_IsSet() {
        License license = openAPI.getInfo().getLicense();
        assertThat(license.getUrl()).isNotBlank();
        assertThat(license.getUrl()).contains("opensource");
    }

    @Test
    @DisplayName("Should have at least one server configured")
    void servers_AreConfigured() {
        List<Server> servers = openAPI.getServers();
        assertThat(servers).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Should have local development server")
    void localServer_IsConfigured() {
        List<Server> servers = openAPI.getServers();
        assertThat(servers)
            .anyMatch(server -> server.getUrl().contains("localhost"));
    }

    @Test
    @DisplayName("Local server URL should be correct")
    void localServerUrl_IsCorrect() {
        Server localServer = openAPI.getServers().get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:8080");
    }

    @Test
    @DisplayName("Local server should have description")
    void localServer_HasDescription() {
        Server localServer = openAPI.getServers().get(0);
        assertThat(localServer.getDescription()).isNotBlank();
        assertThat(localServer.getDescription()).containsIgnoringCase("Development");
    }

    @Test
    @DisplayName("OpenAPI configuration should be complete")
    void configuration_IsComplete() {
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getServers()).isNotEmpty();
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getLicense()).isNotNull();
    }
}
