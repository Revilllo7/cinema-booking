package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.Role;
import com.cinema.fixtures.EntityFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RoleRepository using H2 in-memory database.
 * Tests CRUD operations and custom query methods for user roles.
 */
@DataJpaTest
class RoleRepositoryTest extends PostgresTestContainer {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidRole_PersistsToDatabase() {
        // Given
        Role role = Role.builder()
            .name("ROLE_MANAGER")
            .description("Manager role")
            .build();

        // When
        Role saved = roleRepository.save(role);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("ROLE_MANAGER");
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingRole_ReturnsRole() {
        // Given
        Role role = entityManager.persist(EntityFixtures.createUserRole());
        entityManager.flush();

        // When
        Optional<Role> found = roleRepository.findById(role.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void findById_NonExistingRole_ReturnsEmpty() {
        // When
        Optional<Role> found = roleRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByName_ExistingName_ReturnsRole() {
        // Given
        entityManager.persist(EntityFixtures.createAdminRole());
        entityManager.flush();

        // When
        Optional<Role> found = roleRepository.findByName("ROLE_ADMIN");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void findByName_NonExistingName_ReturnsEmpty() {
        // When
        Optional<Role> found = roleRepository.findByName("ROLE_NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByName_ExistingName_ReturnsTrue() {
        // Given
        entityManager.persist(EntityFixtures.createUserRole());
        entityManager.flush();

        // When
        boolean exists = roleRepository.existsByName("ROLE_USER");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_NonExistingName_ReturnsFalse() {
        // When
        boolean exists = roleRepository.existsByName("ROLE_NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingRole_PersistsChanges() {
        // Given
        Role role = entityManager.persist(EntityFixtures.createUserRole());
        entityManager.flush();
        entityManager.clear();

        // When
        Role toUpdate = roleRepository.findById(role.getId()).orElseThrow();
        toUpdate.setDescription("Updated user role description");
        Role updated = roleRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getDescription()).isEqualTo("Updated user role description");
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingRole_RemovesFromDatabase() {
        // Given
        Role role = entityManager.persist(EntityFixtures.createUserRole());
        entityManager.flush();
        Long roleId = role.getId();

        // When
        roleRepository.deleteById(roleId);
        entityManager.flush();

        // Then
        assertThat(roleRepository.findById(roleId)).isEmpty();
    }

    @Test
    void existsById_ExistingRole_ReturnsTrue() {
        // Given
        Role role = entityManager.persist(EntityFixtures.createAdminRole());
        entityManager.flush();

        // When
        boolean exists = roleRepository.existsById(role.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingRole_ReturnsFalse() {
        // When
        boolean exists = roleRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        for (int i = 0; i < 15; i++) {
            entityManager.persist(Role.builder()
                .name("ROLE_" + i)
                .description("Role " + i)
                .build());
        }
        entityManager.flush();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        // When
        org.springframework.data.domain.Page<Role> page = roleRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_SecondPage_ReturnsRemainingElements() {
        // Given
        for (int i = 0; i < 15; i++) {
            entityManager.persist(Role.builder()
                .name("ROLE_" + i)
                .description("Role " + i)
                .build());
        }
        entityManager.flush();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(1, 10);

        // When
        org.springframework.data.domain.Page<Role> page = roleRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(5);
    }

    // ========== MULTI-READ Tests ==========

    @Test
    void findAll_WithMultipleRoles_ReturnsAllRoles() {
        // Given
        entityManager.persist(EntityFixtures.createUserRole());
        entityManager.persist(EntityFixtures.createAdminRole());
        entityManager.persist(Role.builder()
            .name("ROLE_MANAGER")
            .description("Manager role")
            .build());
        entityManager.flush();

        // When
        var allRoles = roleRepository.findAll();

        // Then
        assertThat(allRoles).hasSize(3);
    }
}
