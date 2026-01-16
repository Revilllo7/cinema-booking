package com.cinema.controller.rest;

import com.cinema.config.SecurityConfig;
import com.cinema.dto.DailySalesDTO;
import com.cinema.repository.jdbc.BookingJdbcRepository;
import com.cinema.service.StatsCsvExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminStatsRestController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class AdminStatsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingJdbcRepository bookingJdbcRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private StatsCsvExportService statsCsvExportService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDailySales_ForSpecificMonth_UsesJdbcRepository() throws Exception {
        List<DailySalesDTO> sales = List.of(new DailySalesDTO(LocalDate.now(), 100.0, 20L));
        given(bookingJdbcRepository.getDailySalesForMonth(2024, 5)).willReturn(sales);

        mockMvc.perform(get("/api/v1/admin/stats/sales")
                .param("year", "2024")
                .param("month", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].totalRevenue").value(100.0));

        then(bookingJdbcRepository).should().getDailySalesForMonth(2024, 5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDailySales_ForDateRange_DelegatesToRangeQuery() throws Exception {
        List<DailySalesDTO> sales = List.of(new DailySalesDTO(LocalDate.now(), 100.0, 20L));
        given(bookingJdbcRepository.getDailySalesForDateRange(any(LocalDate.class), any(LocalDate.class))).willReturn(sales);

        mockMvc.perform(get("/api/v1/admin/stats/sales")
                .param("startDate", "2024-05-01")
                .param("endDate", "2024-05-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].ticketsSold").value(20));

        then(bookingJdbcRepository).should().getDailySalesForDateRange(eq(LocalDate.parse("2024-05-01")), eq(LocalDate.parse("2024-05-31")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCurrentMonthSales_ReturnsData() throws Exception {
        List<DailySalesDTO> sales = List.of(new DailySalesDTO(LocalDate.now(), 100.0, 20L));
        given(bookingJdbcRepository.getDailySalesForMonth(anyInt(), anyInt())).willReturn(sales);

        mockMvc.perform(get("/api/v1/admin/stats/sales/current-month"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].date").exists());
    }

    @Test
    void getDailySales_Unauthenticated_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats/sales"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void downloadDailySalesCsv_ReturnsAttachment() throws Exception {
        List<DailySalesDTO> sales = List.of(new DailySalesDTO(LocalDate.of(2024, 5, 1), 120.0, 12L));
        given(bookingJdbcRepository.getDailySalesForMonth(2024, 5)).willReturn(sales);
        given(statsCsvExportService.exportDailySales(sales)).willReturn("csv-body".getBytes());

        mockMvc.perform(get("/api/v1/admin/stats/sales/csv")
                .param("year", "2024")
                .param("month", "5"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("daily-sales-2024-05.csv")))
            .andExpect(content().contentType("text/csv"))
            .andExpect(content().string("csv-body"));

        then(statsCsvExportService).should().exportDailySales(sales);
    }
}
