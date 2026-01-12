package com.cinema.repository;
import com.cinema.support.PostgresTestContainer;

import com.cinema.entity.Role;
import com.cinema.entity.User;
import com.cinema.fixtures.EntityFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository using H2 in-memory database.
 * Tests CRUD operations, custom query methods, and pagination.
 */
@DataJpaTest
class UserRepositoryTest extends PostgresTestContainer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    // ========== CREATE Tests ==========

    @Test
    void save_ValidUser_PersistsToDatabase() {
        // Given
        User user = EntityFixtures.userBuilder()
            .username("newuser")
            .email("newuser@example.com")
            .build();

        // When
        User saved = userRepository.save(user);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_UserWithRole_PersistsRelationship() {
        // Given
        Role role = roleRepository.save(EntityFixtures.createUserRole());
        User user = EntityFixtures.userBuilder()
            .username("roleuser")
            .email("roleuser@example.com")
            .build();
        user.getRoles().add(role);

        // When
        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getRoles()).hasSize(1);
    }

    // ========== READ Tests ==========

    @Test
    void findById_ExistingUser_ReturnsUser() {
        // Given
        User user = EntityFixtures.userBuilder().build();  // No ID - let DB generate
        user = entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findById(user.getId());

        // Then
        assertThat(found).isPresent();
    }

    @Test
    void findById_NonExistingUser_ReturnsEmpty() {
        // When
        Optional<User> found = userRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByUsername_ExistingUsername_ReturnsUser() {
        // Given
        entityManager.persist(EntityFixtures.createDefaultUser());
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_NonExistingUsername_ReturnsEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsUser() {
        // Given
        entityManager.persist(EntityFixtures.createDefaultUser());
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findActiveUserByUsername_EnabledUser_ReturnsUser() {
        // Given
        entityManager.persist(EntityFixtures.createDefaultUser());
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findActiveUserByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEnabled()).isTrue();
    }

    @Test
    void findActiveUserByUsername_DisabledUser_ReturnsEmpty() {
        // Given
        User disabledUser = EntityFixtures.createDisabledUser();
        entityManager.persist(disabledUser);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findActiveUserByUsername("disabled");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findActiveUserByEmail_EnabledUser_ReturnsUser() {
        // Given
        entityManager.persist(EntityFixtures.createDefaultUser());
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findActiveUserByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
    }

    @Test
    void findActiveUserByEmail_DisabledUser_ReturnsEmpty() {
        // Given
        User disabledUser = EntityFixtures.createDisabledUser();
        entityManager.persist(disabledUser);
        entityManager.flush();

        // When
        Optional<User> found = userRepository.findActiveUserByEmail("disabled@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEnabledTrue_WithEnabledUsers_ReturnsPaginatedResults() {
        // Given
        entityManager.persist(EntityFixtures.createDefaultUser());
        entityManager.persist(EntityFixtures.createAdminUser());
        entityManager.persist(EntityFixtures.createDisabledUser());
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findByEnabledTrue(pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByRoleName_UsersWithRole_ReturnsMatchingUsers() {
        // Given
        Role adminRole = roleRepository.save(EntityFixtures.createAdminRole());
        User adminUser = EntityFixtures.createAdminUser();
        adminUser.getRoles().clear();
        adminUser.getRoles().add(adminRole);
        entityManager.persist(adminUser);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> result = userRepository.findByRoleName("ROLE_ADMIN", pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ========== UPDATE Tests ==========

    @Test
    void save_UpdateExistingUser_PersistsChanges() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        entityManager.flush();
        entityManager.clear();

        // When
        User toUpdate = userRepository.findById(user.getId()).orElseThrow();
        toUpdate.setFirstName("Updated");
        User updated = userRepository.save(toUpdate);
        entityManager.flush();

        // Then
        assertThat(updated.getFirstName()).isEqualTo("Updated");
    }

    // ========== DELETE Tests ==========

    @Test
    void deleteById_ExistingUser_RemovesFromDatabase() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        entityManager.flush();
        Long userId = user.getId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    void existsById_ExistingUser_ReturnsTrue() {
        // Given
        User user = entityManager.persist(EntityFixtures.createDefaultUser());
        entityManager.flush();

        // When
        boolean exists = userRepository.existsById(user.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingUser_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    // ========== PAGINATION Tests ==========

    @Test
    void findAll_WithPagination_ReturnsCorrectPageSize() {
        // Given
        for (int i = 0; i < 15; i++) {
            User user = EntityFixtures.userBuilder()
                .username("user" + i)
                .email("user" + i + "@example.com")
                .build();
            entityManager.persist(user);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> page = userRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    void findAll_WithPagination_ReturnsTotalElements() {
        // Given
        for (int i = 0; i < 15; i++) {
            User user = EntityFixtures.userBuilder()
                .username("user" + i)
                .email("user" + i + "@example.com")
                .build();
            entityManager.persist(user);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<User> page = userRepository.findAll(pageable);

        // Then
        assertThat(page.getTotalElements()).isEqualTo(15);
    }
}
