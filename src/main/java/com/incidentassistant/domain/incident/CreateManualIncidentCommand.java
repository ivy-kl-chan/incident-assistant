package com.incidentassistant.domain.incident;

public record CreateManualIncidentCommand(String title, String description, IncidentSeverity severity) {}
