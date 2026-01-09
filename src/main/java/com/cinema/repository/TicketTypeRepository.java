package com.cinema.repository;

import com.cinema.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByActiveTrueOrderByPriceModifier();

    Optional<TicketType> findByName(String name);

    Optional<TicketType> findByIdAndActiveTrue(Long id);
}
