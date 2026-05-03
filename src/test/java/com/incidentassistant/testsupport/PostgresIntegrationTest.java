package com.incidentassistant.testsupport;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Shared PostgreSQL (Testcontainers) for Spring Boot integration tests. Flyway {@code V1} runs on
 * context startup per {@code application.yml}.
 *
 * <p>{@link ServiceConnection} wires JDBC details before the DataSource/Flyway beans initialize,
 * avoiding ordering gaps that can occur when using only {@code @DynamicPropertySource} with
 * Testcontainers-managed containers (see Spring Boot reference — Testcontainers, service connections).
 *
 * <p>{@link DirtiesContext} avoids reusing a cached {@link org.springframework.context.ApplicationContext}
 * whose JDBC URL points at a PostgreSQL instance that Testcontainers has already replaced when
 * multiple {@code @SpringBootTest} subclasses run in one JVM.
 */
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public abstract class PostgresIntegrationTest {

  @Container
  @ServiceConnection
  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine");
}
