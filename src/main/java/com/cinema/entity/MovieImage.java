// Entity class for MovieImage

package com.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(length = 200)
    private String caption;
}
