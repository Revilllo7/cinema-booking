// Entity class for Hall

package com.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "halls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "rows_count", nullable = false)
    private Integer rowsCount;

    @Column(name = "seats_per_row", nullable = false)
    private Integer seatsPerRow;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Seat> seats = new HashSet<>();

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Screening> screenings = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
