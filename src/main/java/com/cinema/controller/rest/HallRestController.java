package com.cinema.controller.rest;

import com.cinema.dto.HallDTO;
import com.cinema.entity.Hall;
import com.cinema.repository.HallRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/halls")
@RequiredArgsConstructor
@Tag(name = "Halls", description = "Cinema hall management endpoints")
public class HallRestController {

    private final HallRepository hallRepository;

    @GetMapping
    @Operation(summary = "Get all halls", description = "Retrieve a paginated list of all cinema halls")
    public ResponseEntity<Page<HallDTO>> getAllHalls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort[1]), sort[0]));
        Page<Hall> halls = hallRepository.findAll(pageable);
        
        Page<HallDTO> hallDTOs = halls.map(this::convertToDTO);
        return ResponseEntity.ok(hallDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hall by ID", description = "Retrieve a specific hall by its ID")
    public ResponseEntity<HallDTO> getHallById(@PathVariable Long id) {
        return hallRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private HallDTO convertToDTO(Hall hall) {
        return HallDTO.builder()
                .id(hall.getId())
                .name(hall.getName())
                .capacity(hall.getTotalSeats())
                .rowsCount(hall.getRowsCount())
                .seatsPerRow(hall.getSeatsPerRow())
                .description(hall.getDescription())
                .build();
    }
}
