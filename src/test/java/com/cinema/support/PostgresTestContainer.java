package com.cinema.support;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared base configuration that starts a PostgreSQL Testcontainer and wires
 * Spring datasource properties for JPA tests. Extend this class in @DataJpaTest
 * classes to run against a real PostgreSQL instance.
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(Lifecycle.PER_CLASS)
public abstract class PostgresTestContainer {

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("cinema_test")
            .withUsername("test")
            .withPassword("test");

    static {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        // Ensure proper cleanup to avoid resource leak warnings
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (POSTGRES.isRunning()) {
                POSTGRES.stop();
            }
        }));
    }

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        // Ensure app-level SQL init doesn't interfere with tests
        registry.add("spring.sql.init.mode", () -> "never");
    }
}
