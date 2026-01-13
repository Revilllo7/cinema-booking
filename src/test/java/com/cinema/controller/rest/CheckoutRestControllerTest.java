package com.cinema.controller.rest;

import com.cinema.dto.CheckoutRequest;
import com.cinema.dto.CheckoutResponse;
import com.cinema.service.CheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CheckoutRestController.class)
@AutoConfigureMockMvc
class CheckoutRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckoutService checkoutService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(username = "jane")
    void checkout_WithAuthenticatedUser_ReturnsConfirmation() throws Exception {
        CheckoutResponse response = CheckoutResponse.builder()
            .bookingNumber("BOOK-123")
            .paymentReference("PAY-123")
            .totalPrice(50.0)
            .qrCodeImage("qr")
            .items(java.util.Collections.emptyList())
            .build();
        given(checkoutService.finalizeCheckout(anyLong(), any(CheckoutRequest.class), anyString(), eq("jane")))
            .willReturn(response);

        CheckoutRequest payload = new CheckoutRequest("buyer@example.com", "", "Jane Doe", "CARD");

        mockMvc.perform(post("/api/v1/screenings/7/checkout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookingNumber").value("BOOK-123"))
            .andExpect(jsonPath("$.qrCodeImage").value("qr"));

        then(checkoutService).should().finalizeCheckout(eq(7L), any(CheckoutRequest.class), anyString(), eq("jane"));
    }

    @Test
    @WithAnonymousUser
    void checkout_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        CheckoutRequest payload = new CheckoutRequest("buyer@example.com", "", "Jane Doe", "CARD");

        mockMvc.perform(post("/api/v1/screenings/7/checkout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "jane")
    void checkout_WithInvalidPayload_ReturnsBadRequest() throws Exception {
        CheckoutRequest payload = new CheckoutRequest("invalid-email", "12", "", "");

        mockMvc.perform(post("/api/v1/screenings/7/checkout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(checkoutService);
    }
}
