package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.TicketType;
import com.cinema.fixtures.EntityFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TicketTypeRepository using H2 in-memory database.
 * Tests CRUD operations and custom query methods for ticket types.
 */
@DataJpaTest
class TicketTypeRepositoryTest extends PostgresTestContainer {

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidTicketType_PersistsToDatabase() {
        // Given
        TicketType ticketType = TicketType.builder()
            .name("Child")
            .description("Child ticket")
            .priceModifier(0.5)
            .active(true)
            .build();

        // When
        TicketType saved = ticketTypeRepository.save(ticketType);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Child");
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingTicketType_ReturnsTicketType() {
        // Given
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());
        entityManager.flush();

        // When
        Optional<TicketType> found = ticketTypeRepository.findById(ticketType.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Normal");
    }

    @Test
    void findById_NonExistingTicketType_ReturnsEmpty() {
        // When
        Optional<TicketType> found = ticketTypeRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_ExistingName_ReturnsTicketType() {
        // Given
        entityManager.persist(EntityFixtures.createStudentTicket());
        entityManager.flush();

        // When
        Optional<TicketType> found = ticketTypeRepository.findByName("Student");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Student");
    }

    @Test
    void findByName_NonExistingName_ReturnsEmpty() {
        // When
        Optional<TicketType> found = ticketTypeRepository.findByName("NonExistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByIdAndActiveTrue_ActiveTicketType_ReturnsTicketType() {
        // Given
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());
        entityManager.flush();

        // When
        Optional<TicketType> found = ticketTypeRepository.findByIdAndActiveTrue(ticketType.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getActive()).isTrue();
    }

    @Test
    void findByIdAndActiveTrue_InactiveTicketType_ReturnsEmpty() {
        // Given
        TicketType inactiveTicket = TicketType.builder()
            .name("Archive")
            .description("Old ticket type")
            .priceModifier(1.0)
            .active(false)
            .build();
        TicketType ticketType = entityManager.persist(inactiveTicket);
        entityManager.flush();

        // When
        Optional<TicketType> found = ticketTypeRepository.findByIdAndActiveTrue(ticketType.getId());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByActiveTrueOrderByPriceModifier_WithActiveTickets_ReturnsOrderedResults() {
        // Given
        entityManager.persist(EntityFixtures.createStudentTicket());  // 0.7
        entityManager.persist(EntityFixtures.createNormalTicket());   // 1.0
        entityManager.persist(EntityFixtures.createSeniorTicket());   // 0.75
        entityManager.persist(TicketType.builder()
            .name("Archive")
            .description("Old ticket")
            .priceModifier(0.8)
            .active(false)
            .build());
        entityManager.flush();

        // When
        List<TicketType> result = ticketTypeRepository.findByActiveTrueOrderByPriceModifier();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPriceModifier()).isEqualTo(0.7);
        assertThat(result.get(1).getPriceModifier()).isEqualTo(0.75);
        assertThat(result.get(2).getPriceModifier()).isEqualTo(1.0);
    }

    @Test
    void findByActiveTrueOrderByPriceModifier_NoActiveTickets_ReturnsEmptyList() {
        // Given
        entityManager.persist(TicketType.builder()
            .name("Inactive1")
            .description("Inactive")
            .priceModifier(1.0)
            .active(false)
            .build());
        entityManager.persist(TicketType.builder()
            .name("Inactive2")
            .description("Inactive")
            .priceModifier(0.8)
            .active(false)
            .build());
        entityManager.flush();

        // When
        List<TicketType> result = ticketTypeRepository.findByActiveTrueOrderByPriceModifier();

        // Then
        assertThat(result).isEmpty();
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingTicketType_PersistsChanges() {
        // Given
        TicketType ticketType = entityManager.persist(EntityFixtures.createNormalTicket());
        entityManager.flush();
        entityManager.clear();

        // When
        TicketType toUpdate = ticketTypeRepository.findById(ticketType.getId()).orElseThrow();
        toUpdate.setDescription("Updated description");
        toUpdate.setPriceModifier(0.9);
        TicketType updated = ticketTypeRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getPriceModifier()).isEqualTo(0.9);
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingTicketType_RemovesFromDatabase() {
        // Given
        TicketType ticketType = entityManager.persist(EntityFixtures.createStudentTicket());
        entityManager.flush();
        Long ticketTypeId = ticketType.getId();

        // When
        ticketTypeRepository.deleteById(ticketTypeId);
        entityManager.flush();

        // Then
        assertThat(ticketTypeRepository.findById(ticketTypeId)).isEmpty();
    }

    @Test
    void existsById_ExistingTicketType_ReturnsTrue() {
        // Given
        TicketType ticketType = entityManager.persist(EntityFixtures.createSeniorTicket());
        entityManager.flush();

        // When
        boolean exists = ticketTypeRepository.existsById(ticketType.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingTicketType_ReturnsFalse() {
        // When
        boolean exists = ticketTypeRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        for (int i = 0; i < 15; i++) {
            entityManager.persist(TicketType.builder()
                .name("Ticket Type " + i)
                .description("Description " + i)
                .priceModifier(1.0 - (i * 0.05))
                .active(true)
                .build());
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TicketType> page = ticketTypeRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_SecondPage_ReturnsRemainingElements() {
        // Given
        for (int i = 0; i < 15; i++) {
            entityManager.persist(TicketType.builder()
                .name("Ticket Type " + i)
                .description("Description " + i)
                .priceModifier(1.0 - (i * 0.05))
                .active(true)
                .build());
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(1, 10);

        // When
        Page<TicketType> page = ticketTypeRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
    }

    // ========== MULTI-READ Tests ==========

    @Test
    void findAll_WithMultipleTicketTypes_ReturnsAllTypes() {
        // Given
        entityManager.persist(EntityFixtures.createNormalTicket());
        entityManager.persist(EntityFixtures.createStudentTicket());
        entityManager.persist(EntityFixtures.createSeniorTicket());
        entityManager.flush();

        // When
        var allTickets = ticketTypeRepository.findAll();

        // Then
        assertThat(allTickets).hasSizeGreaterThanOrEqualTo(3);
    }
}
