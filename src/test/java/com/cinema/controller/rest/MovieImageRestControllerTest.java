package com.cinema.controller.rest;

import com.cinema.config.SecurityConfig;
import com.cinema.dto.MovieImageDTO;
import com.cinema.service.MovieImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieImageRestController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class MovieImageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieImageService movieImageService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getGallery_ReturnsImages() throws Exception {
        List<MovieImageDTO> gallery = List.of(MovieImageDTO.builder().id(1L).imagePath("/uploads/1").displayOrder(1).build());
        given(movieImageService.getGallery(9L)).willReturn(gallery);

        mockMvc.perform(get("/api/v1/movies/9/images"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].imagePath").value("/uploads/1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadImages_AsAdmin_CreatesGallery() throws Exception {
        List<MovieImageDTO> gallery = List.of(MovieImageDTO.builder().id(1L).imagePath("/uploads/1").displayOrder(1).build());
        given(movieImageService.addImages(anyLong(), any(), any())).willReturn(gallery);

        MockMultipartFile file = new MockMultipartFile("files", "pic.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1});

        mockMvc.perform(multipart("/api/v1/movies/9/images")
                .file(file)
                .param("captions", "Poster")
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].id").value(1));

        then(movieImageService).should().addImages(eq(9L), any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void uploadImages_AsUser_ReturnsForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "pic.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1});

        mockMvc.perform(multipart("/api/v1/movies/9/images")
                .file(file)
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteImage_AsAdmin_DelegatesToService() throws Exception {
        mockMvc.perform(delete("/api/v1/movies/9/images/3").with(csrf()))
            .andExpect(status().isNoContent());

        then(movieImageService).should().deleteImage(9L, 3L);
    }
}
