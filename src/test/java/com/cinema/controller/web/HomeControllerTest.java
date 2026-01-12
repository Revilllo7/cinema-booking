package com.cinema.controller.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HomeController using @SpringBootTest with MockMvc.
 * Tests web endpoints for correct view names and model attributes.
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("HomeController Tests")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== GET / - Home Page ==========

    @Nested
    @DisplayName("GET / Tests")
    class RootPathTests {

        @Test
        @DisplayName("Should return index view for root path")
        @WithAnonymousUser
        void getRoot_ReturnsIndexView() throws Exception {
            mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Home"));
        }

        @Test
        @DisplayName("Should be publicly accessible")
        @WithAnonymousUser
        void getRoot_Unauthenticated_Allowed() throws Exception {
            mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should set correct page title in model")
        @WithMockUser
        void getRoot_SetCorrectPageTitle() throws Exception {
            mockMvc.perform(get("/"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Home"));
        }
    }

    // ========== GET /home - Home Page Alternative ==========

    @Nested
    @DisplayName("GET /home Tests")
    class HomePathTests {

        @Test
        @DisplayName("Should return index view for /home path")
        @WithAnonymousUser
        void getHome_ReturnsIndexView() throws Exception {
            mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("pageTitle", "Home"));
        }

        @Test
        @DisplayName("Should return same view as root path")
        @WithAnonymousUser
        void getHome_SameAsRootPath() throws Exception {
            mockMvc.perform(get("/home"))
                .andExpect(view().name("index"));
        }

        @Test
        @DisplayName("Should be publicly accessible")
        @WithAnonymousUser
        void getHome_Unauthenticated_Allowed() throws Exception {
            mockMvc.perform(get("/home"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should be accessible to authenticated users")
        @WithMockUser
        void getHome_Authenticated_Allowed() throws Exception {
            mockMvc.perform(get("/home"))
                .andExpect(status().isOk());
        }
    }

    // ========== GET /about - About Page ==========

    @Nested
    @DisplayName("GET /about Tests")
    class AboutPathTests {

        @Test
        @DisplayName("Should return about view")
        @WithAnonymousUser
        void getAbout_ReturnsAboutView() throws Exception {
            mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "About Us"));
        }

        @Test
        @DisplayName("Should set correct page title in model")
        @WithAnonymousUser
        void getAbout_SetCorrectPageTitle() throws Exception {
            mockMvc.perform(get("/about"))
                .andExpect(model().attribute("pageTitle", "About Us"));
        }

        @Test
        @DisplayName("Should be publicly accessible")
        @WithAnonymousUser
        void getAbout_Unauthenticated_Allowed() throws Exception {
            mockMvc.perform(get("/about"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should be accessible to authenticated users")
        @WithMockUser
        void getAbout_Authenticated_Allowed() throws Exception {
            mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
        }

        @Test
        @DisplayName("Should return 200 OK status")
        @WithMockUser(roles = "ADMIN")
        void getAbout_ReturnsOkStatus() throws Exception {
            mockMvc.perform(get("/about"))
                .andExpect(status().isOk());
        }
    }

    // ========== GET /contact - Contact Page ==========

    @Nested
    @DisplayName("GET /contact Tests")
    class ContactPathTests {

        @Test
        @DisplayName("Should return contact view")
        @WithAnonymousUser
        void getContact_ReturnsContactView() throws Exception {
            mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Contact"));
        }

        @Test
        @DisplayName("Should set correct page title in model")
        @WithAnonymousUser
        void getContact_SetCorrectPageTitle() throws Exception {
            mockMvc.perform(get("/contact"))
                .andExpect(model().attribute("pageTitle", "Contact"));
        }

        @Test
        @DisplayName("Should be publicly accessible")
        @WithAnonymousUser
        void getContact_Unauthenticated_Allowed() throws Exception {
            mockMvc.perform(get("/contact"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should be accessible to authenticated users")
        @WithMockUser
        void getContact_Authenticated_Allowed() throws Exception {
            mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
        }

        @Test
        @DisplayName("Should return 200 OK status")
        @WithMockUser(roles = "USER")
        void getContact_ReturnsOkStatus() throws Exception {
            mockMvc.perform(get("/contact"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should be accessible to admin users")
        @WithMockUser(roles = "ADMIN")
        void getContact_AdminAccess_Allowed() throws Exception {
            mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
        }
    }

    // ========== General Page Title Tests ==========

    @Nested
    @DisplayName("Page Title Attribute Tests")
    class PageTitleTests {

        @Test
        @DisplayName("All pages should include pageTitle attribute")
        @WithMockUser
        void allPages_IncludePageTitle() throws Exception {
            // Root
            mockMvc.perform(get("/"))
                .andExpect(model().attributeExists("pageTitle"));

            // Home
            mockMvc.perform(get("/home"))
                .andExpect(model().attributeExists("pageTitle"));

            // About
            mockMvc.perform(get("/about"))
                .andExpect(model().attributeExists("pageTitle"));

            // Contact
            mockMvc.perform(get("/contact"))
                .andExpect(model().attributeExists("pageTitle"));
        }

        @Test
        @DisplayName("Page titles should not be null or empty")
        @WithAnonymousUser
        void pageTitles_NotNullOrEmpty() throws Exception {
            mockMvc.perform(get("/"))
                .andExpect(model().attribute("pageTitle", org.hamcrest.Matchers.not("")))
                .andExpect(model().attribute("pageTitle", org.hamcrest.Matchers.notNullValue()));

            mockMvc.perform(get("/about"))
                .andExpect(model().attribute("pageTitle", org.hamcrest.Matchers.not("")));

            mockMvc.perform(get("/contact"))
                .andExpect(model().attribute("pageTitle", org.hamcrest.Matchers.not("")));
        }
    }

    // ========== View Name Tests ==========

    @Nested
    @DisplayName("View Name Tests")
    class ViewNameTests {

        @Test
        @DisplayName("Should resolve to correct template files")
        @WithAnonymousUser
        void viewNames_MatchTemplateFiles() throws Exception {
            mockMvc.perform(get("/")).andExpect(view().name("index"));
            mockMvc.perform(get("/home")).andExpect(view().name("index"));
            mockMvc.perform(get("/about")).andExpect(view().name("about"));
            mockMvc.perform(get("/contact")).andExpect(view().name("contact"));
        }

        @Test
        @DisplayName("Home and root paths should return same view")
        @WithAnonymousUser
        void homeAndRoot_ReturnSameView() throws Exception {
            mockMvc.perform(get("/")).andExpect(view().name("index"));
            mockMvc.perform(get("/home")).andExpect(view().name("index"));
        }
    }

    // ========== HTTP Status Tests ==========

    @Nested
    @DisplayName("HTTP Status Code Tests")
    class HttpStatusTests {

        @Test
        @DisplayName("All pages should return 200 OK")
        @WithAnonymousUser
        void allPages_Return200Ok() throws Exception {
            mockMvc.perform(get("/")).andExpect(status().isOk());
            mockMvc.perform(get("/home")).andExpect(status().isOk());
            mockMvc.perform(get("/about")).andExpect(status().isOk());
            mockMvc.perform(get("/contact")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("Invalid paths should return 404 Not Found")
        @WithAnonymousUser
        void invalidPath_Returns404() throws Exception {
            mockMvc.perform(get("/invalid-path"))
                .andExpect(status().isNotFound());
        }
    }
}
