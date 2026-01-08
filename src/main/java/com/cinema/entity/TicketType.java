// Entity class for TicketType

package com.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ticket_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_modifier", nullable = false)
    private Double priceModifier;

    @OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<BookingSeat> bookingSeats = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
