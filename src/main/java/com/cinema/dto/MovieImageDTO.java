package com.cinema.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MovieImageDTO {
    Long id;
    String imagePath;
    Integer displayOrder;
    String caption;
}
