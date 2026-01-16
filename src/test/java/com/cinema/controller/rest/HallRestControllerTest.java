package com.cinema.controller.rest;

import com.cinema.config.SecurityConfig;
import com.cinema.entity.Hall;
import com.cinema.repository.HallRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HallRestController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class HallRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HallRepository hallRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getAllHalls_ReturnsPagedResponse() throws Exception {
        Hall hall = Hall.builder().id(1L).name("Main Hall").totalSeats(100).rowsCount(10).seatsPerRow(10).description("Desc").build();
        Page<Hall> page = new PageImpl<>(List.of(hall));
        given(hallRepository.findAll(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/v1/halls").param("page", "0").param("size", "5").param("sort", "id,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Main Hall"))
            .andExpect(jsonPath("$.content[0].rowsCount").value(10));
    }

    @Test
    void getHallById_WhenPresent_ReturnsHall() throws Exception {
        Hall hall = Hall.builder().id(1L).name("Main Hall").totalSeats(100).rowsCount(10).seatsPerRow(10).description("Desc").build();
        given(hallRepository.findById(1L)).willReturn(Optional.of(hall));

        mockMvc.perform(get("/api/v1/halls/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.capacity").value(100));
    }

    @Test
    void getHallById_WhenMissing_ReturnsNotFound() throws Exception {
        given(hallRepository.findById(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/halls/99"))
            .andExpect(status().isNotFound());
    }
}
