package com.incidentassistant.web.incident;

import com.incidentassistant.domain.incident.IncidentNotFoundException;
import com.incidentassistant.domain.incident.IncidentValidationException;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Minimal JSON errors for Phase 1a; RFC 7807 Problem Details are deferred ({@code Story 7}).
 *
 * <p>Maps persistence failures to {@code 503} per {@code api-contract.md} availability clauses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(IncidentNotFoundException.class)
  public ResponseEntity<Map<String, String>> notFound(IncidentNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", "Incident not found"));
  }

  @ExceptionHandler(IncidentValidationException.class)
  public ResponseEntity<Map<String, String>> badRequest(IncidentValidationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("detail", ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> badJson(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("detail", "Invalid JSON request body"));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Map<String, String>> unsupportedMedia(HttpMediaTypeNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(Map.of("detail", "Content-Type must be application/json for this request"));
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<Map<String, String>> persistenceUnavailable(DataAccessException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Map.of("detail", "Persistence layer unavailable"));
  }
}
