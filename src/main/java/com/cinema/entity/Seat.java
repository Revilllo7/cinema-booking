// Entity class for Seat

package com.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"hall_id", "row_number", "seat_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    @Builder.Default
    private SeatType seatType = SeatType.STANDARD;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<BookingSeat> bookingSeats = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public enum SeatType {
        STANDARD,
        COMFORT,
        VIP,
        WHEELCHAIR
    }
}
