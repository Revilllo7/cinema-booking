package com.cinema.controller.rest;

import com.cinema.dto.MovieDTO;
import com.cinema.exception.ResourceNotFoundException;
import com.cinema.fixtures.ControllerTestFixtures;
import com.cinema.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("MovieRestController Tests")
class MovieRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    private MovieDTO validMovieDTO;

    @BeforeEach
    void setUp() {
        validMovieDTO = ControllerTestFixtures.createValidMovieDTOForCreation();
    }

    @Nested
    @DisplayName("GET /api/v1/movies")
    class GetAllMovies {

        @Test
        @DisplayName("Should return paged movies")
        void getAllMovies_ReturnsPage() throws Exception {
            Page<MovieDTO> page = new PageImpl<>(List.of(validMovieDTO));
            when(movieService.getAllActiveMovies(any())).thenReturn(page);

            mockMvc.perform(get("/api/v1/movies")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(movieService).getAllActiveMovies(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/movies/{id}")
    class GetMovieById {

        @Test
        @DisplayName("Should return movie by ID")
        void getMovieById_ReturnsMovie() throws Exception {
            MovieDTO movie = validMovieDTO;
            movie.setId(1L);
            when(movieService.getMovieById(1L)).thenReturn(movie);

            mockMvc.perform(get("/api/v1/movies/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(movieService).getMovieById(1L);
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void getMovieById_NotFound() throws Exception {
            when(movieService.getMovieById(999L))
                    .thenThrow(new ResourceNotFoundException("Movie not found"));

            mockMvc.perform(get("/api/v1/movies/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(movieService).getMovieById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/movies/search")
    class SearchMovies {

        @Test
        @DisplayName("Should search movies by keyword")
        void searchMovies_ByKeyword() throws Exception {
            Page<MovieDTO> page = new PageImpl<>(List.of(validMovieDTO));
            when(movieService.searchMoviesByTitle(eq("Test"), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/movies/search")
                    .param("keyword", "Test")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(movieService).searchMoviesByTitle(eq("Test"), any());
        }

        @Test
        @DisplayName("Should return empty page when no matches")
        void searchMovies_NoMatches() throws Exception {
            Page<MovieDTO> emptyPage = new PageImpl<>(List.of());
            when(movieService.searchMoviesByTitle(eq("None"), any()))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/movies/search")
                    .param("keyword", "None")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(movieService).searchMoviesByTitle(eq("None"), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/movies/genre/{genre}")
    class GetMoviesByGenre {

        @Test
        @DisplayName("Should return movies by genre")
        void getMoviesByGenre_ReturnsPage() throws Exception {
            Page<MovieDTO> page = new PageImpl<>(List.of(validMovieDTO));
            when(movieService.getMoviesByGenre(eq("Action"), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/movies/genre/Action")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(movieService).getMoviesByGenre(eq("Action"), any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/movies/genres")
    class GetGenres {

        @Test
        @DisplayName("Should return all genres")
        void getGenres_ReturnsList() throws Exception {
            when(movieService.getAllGenres()).thenReturn(List.of("Action", "Drama"));

            mockMvc.perform(get("/api/v1/movies/genres")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(movieService).getAllGenres();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/movies")
    class CreateMovie {

        @Test
        @DisplayName("Admin can create movie")
        @WithMockUser(roles = "ADMIN")
        void createMovie_Admin_Success() throws Exception {
            MovieDTO created = validMovieDTO;
            created.setId(1L);
            when(movieService.createMovie(any(MovieDTO.class))).thenReturn(created);

            mockMvc.perform(post("/api/v1/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validMovieDTO)))
                    .andExpect(status().isCreated());

            verify(movieService).createMovie(any(MovieDTO.class));
        }

        @Test
        @DisplayName("User cannot create movie")
        @WithMockUser(roles = "USER")
        void createMovie_User_Forbidden() throws Exception {
            mockMvc.perform(post("/api/v1/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validMovieDTO)))
                    .andExpect(status().isForbidden());

            verify(movieService, never()).createMovie(any(MovieDTO.class));
        }

        @Test
        @DisplayName("Anonymous cannot create movie")
        @WithAnonymousUser
        void createMovie_Anonymous_Unauthorized() throws Exception {
            mockMvc.perform(post("/api/v1/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validMovieDTO)))
                    .andExpect(status().isUnauthorized());

            verify(movieService, never()).createMovie(any(MovieDTO.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/movies/{id}")
    class UpdateMovie {

        @Test
        @DisplayName("Admin can update movie")
        @WithMockUser(roles = "ADMIN")
        void updateMovie_Admin_Success() throws Exception {
            MovieDTO updated = validMovieDTO;
            updated.setId(1L);
            updated.setTitle("Updated Title");
            when(movieService.updateMovie(eq(1L), any(MovieDTO.class))).thenReturn(updated);

            mockMvc.perform(put("/api/v1/movies/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(updated)))
                    .andExpect(status().isOk());

            verify(movieService).updateMovie(eq(1L), any(MovieDTO.class));
        }

        @Test
        @DisplayName("User cannot update movie")
        @WithMockUser(roles = "USER")
        void updateMovie_User_Forbidden() throws Exception {
            mockMvc.perform(put("/api/v1/movies/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validMovieDTO)))
                    .andExpect(status().isForbidden());

            verify(movieService, never()).updateMovie(anyLong(), any(MovieDTO.class));
        }

        @Test
        @DisplayName("Should return 404 when movie not found")
        @WithMockUser(roles = "ADMIN")
        void updateMovie_NotFound() throws Exception {
            when(movieService.updateMovie(eq(999L), any(MovieDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Movie not found"));

            mockMvc.perform(put("/api/v1/movies/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ControllerTestFixtures.toJsonString(validMovieDTO)))
                    .andExpect(status().isNotFound());

            verify(movieService).updateMovie(eq(999L), any(MovieDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/movies/{id}")
    class DeleteMovie {

        @Test
        @DisplayName("Admin can delete movie")
        @WithMockUser(roles = "ADMIN")
        void deleteMovie_Admin_Success() throws Exception {
            doNothing().when(movieService).deleteMovie(1L);

            mockMvc.perform(delete("/api/v1/movies/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(movieService).deleteMovie(1L);
        }

        @Test
        @DisplayName("User cannot delete movie")
        @WithMockUser(roles = "USER")
        void deleteMovie_User_Forbidden() throws Exception {
            mockMvc.perform(delete("/api/v1/movies/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(movieService, never()).deleteMovie(anyLong());
        }

        @Test
        @DisplayName("Should return 404 when movie not found")
        @WithMockUser(roles = "ADMIN")
        void deleteMovie_NotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Movie not found"))
                    .when(movieService).deleteMovie(999L);

            mockMvc.perform(delete("/api/v1/movies/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(movieService).deleteMovie(999L);
        }
    }
}
