package com.incidentassistant;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class IncidentAssistantApplicationTest {

  @Test
  void main_delegatesToSpringApplicationRun() {
    try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
      ConfigurableApplicationContext ctx = org.mockito.Mockito.mock(ConfigurableApplicationContext.class);
      spring
          .when(() -> SpringApplication.run(IncidentAssistantApplication.class, new String[] {}))
          .thenReturn(ctx);

      IncidentAssistantApplication.main(new String[] {});

      spring.verify(() -> SpringApplication.run(IncidentAssistantApplication.class, new String[] {}));
    }
  }
}
