package com.incidentassistant.autoconfigure;

import com.incidentassistant.application.incident.ManualIncidentService;
import com.incidentassistant.domain.incident.ManualIncidentRepository;
import com.incidentassistant.persistence.incident.JdbcManualIncidentRepository;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Registers JDBC-backed manual incident beans after {@link JdbcTemplate} is
 * available. Using
 * auto-configuration (vs {@code @ConditionalOnBean} on
 * {@code @Service}/{@code @Repository})
 * avoids startup ordering where component-scan conditions run before the JDBC
 * auto-configuration
 * registers {@code jdbcTemplate}.
 */
@AutoConfiguration
@AutoConfigureAfter(JdbcTemplateAutoConfiguration.class)
public class IncidentJdbcAutoConfiguration {

  @Bean
  @ConditionalOnBean(JdbcTemplate.class)
  ManualIncidentRepository manualIncidentRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcManualIncidentRepository(jdbcTemplate);
  }

  @Bean
  @ConditionalOnBean(JdbcTemplate.class)
  ManualIncidentService manualIncidentService(
      ManualIncidentRepository manualIncidentRepository, Clock clock) {
    return new ManualIncidentService(manualIncidentRepository, clock);
  }
}
