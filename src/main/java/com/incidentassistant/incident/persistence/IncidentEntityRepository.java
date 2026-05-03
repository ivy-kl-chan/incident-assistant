package com.incidentassistant.incident.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEntityRepository extends JpaRepository<IncidentEntity, UUID> {}
