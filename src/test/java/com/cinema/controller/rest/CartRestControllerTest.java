package com.cinema.controller.rest;

import com.cinema.config.SecurityConfig;
import com.cinema.dto.AddCartItemRequest;
import com.cinema.dto.CartItemResponse;
import com.cinema.dto.CartResponse;
import com.cinema.dto.TicketOptionResponse;
import com.cinema.dto.UpdateCartItemRequest;
import com.cinema.entity.TicketTypeName;
import com.cinema.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartRestController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class CartRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private UserDetailsService userDetailsService;

    private CartResponse sampleCartResponse() {
        CartItemResponse item = CartItemResponse.builder()
            .seatId(1L)
            .rowNumber(2)
            .seatNumber(5)
            .ticketType(TicketTypeName.STANDARD)
            .ticketTypeId(9L)
            .price(25.0)
            .lockExpiresAt(LocalDateTime.now().plusMinutes(5))
            .build();
        return CartResponse.builder()
            .screeningId(4L)
            .items(List.of(item))
            .subtotal(25.0)
            .build();
    }

    @Test
    void getCart_ReturnsResponseFromService() throws Exception {
        given(cartService.getCart(anyLong(), anyString(), isNull())).willReturn(sampleCartResponse());

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(get("/api/v1/screenings/4/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.screeningId").value(4));

        then(cartService).should().getCart(eq(4L), eq(session.getId()), isNull());
    }

    @Test
    void getTicketOptions_ReturnsAvailableTypes() throws Exception {
        List<TicketOptionResponse> options = List.of(
            TicketOptionResponse.builder().ticketTypeId(1L).name(TicketTypeName.STANDARD).price(25.0).build());
        given(cartService.getTicketOptions()).willReturn(options);

        mockMvc.perform(get("/api/v1/screenings/4/cart/ticket-options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].ticketTypeId").value(1));
    }

    @Test
    @WithMockUser(username = "jane")
    void addSeat_UsesSessionAndPrincipal() throws Exception {
        given(cartService.addSeat(anyLong(), anyLong(), anyLong(), anyString(), anyString())).willReturn(sampleCartResponse());
        AddCartItemRequest request = new AddCartItemRequest(7L, 9L);

        mockMvc.perform(post("/api/v1/screenings/4/cart")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].seatId").value(1));

        then(cartService).should().addSeat(eq(4L), eq(7L), eq(9L), anyString(), eq("jane"));
    }

    @Test
    @WithMockUser
    void updateSeat_DelegatesToService() throws Exception {
        given(cartService.updateTicketType(anyLong(), anyLong(), anyLong(), anyString(), anyString())).willReturn(sampleCartResponse());
        UpdateCartItemRequest request = new UpdateCartItemRequest(10L);

        mockMvc.perform(patch("/api/v1/screenings/4/cart/7")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subtotal").value(25.0));

        then(cartService).should().updateTicketType(eq(4L), eq(7L), eq(10L), anyString(), eq("user"));
    }

    @Test
    @WithMockUser(username = "jane")
    void removeSeat_DelegatesToService() throws Exception {
        given(cartService.removeSeat(anyLong(), anyLong(), anyString(), anyString())).willReturn(sampleCartResponse());

        mockMvc.perform(delete("/api/v1/screenings/4/cart/7").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].ticketType").value("STANDARD"));

        then(cartService).should().removeSeat(eq(4L), eq(7L), anyString(), eq("jane"));
    }
}
