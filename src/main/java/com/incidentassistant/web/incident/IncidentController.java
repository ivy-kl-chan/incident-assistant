package com.incidentassistant.web.incident;

import com.fasterxml.jackson.databind.JsonNode;
import com.incidentassistant.application.incident.ManualIncidentService;
import com.incidentassistant.domain.incident.CreateManualIncidentCommand;
import com.incidentassistant.domain.incident.Incident;
import com.incidentassistant.domain.incident.IncidentPage;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

  private final ManualIncidentService incidentService;

  public IncidentController(ManualIncidentService incidentService) {
    this.incidentService = incidentService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<IncidentResponse> create(@RequestBody CreateIncidentRequest body) {
    var created =
        incidentService.create(
            new CreateManualIncidentCommand(body.title(), body.description(), body.severity()));
    return ResponseEntity.status(HttpStatus.CREATED).body(IncidentResponse.from(created));
  }

  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<IncidentResponse> getById(@PathVariable UUID id) {
    Incident incident = incidentService.getForApi(id);
    return ResponseEntity.ok()
        .eTag(String.valueOf(incident.version()))
        .body(IncidentResponse.from(incident));
  }

  @PatchMapping(
      path = "/{id}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<IncidentResponse> patch(
      @PathVariable UUID id,
      @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
      @RequestBody JsonNode body) {
    Incident current = incidentService.getForApi(id);
    IfMatchPrecondition.requireCurrentVersionMatched(ifMatch, current.version());
    Incident updated =
        incidentService.updateFields(id, current.version(), PatchIncidentBodyParser.parse(body));
    return ResponseEntity.ok()
        .eTag(String.valueOf(updated.version()))
        .body(IncidentResponse.from(updated));
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public PagedIncidentsResponse list(
      HttpServletRequest request,
      @RequestParam(required = false) String page,
      @RequestParam(required = false) String size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String sort) {
    IncidentListQueryParser.assertOnlyKnownParameters(request);
    IncidentListQueryParser.ParsedIncidentListQuery q =
        IncidentListQueryParser.parse(page, size, status, sort);
    IncidentPage pageResult =
        incidentService.list(
            q.page(), q.size(), q.statusFilter(), q.sortCreatedAtAscending());
    List<IncidentSummaryResponse> items =
        pageResult.items().stream().map(IncidentSummaryResponse::from).toList();
    return new PagedIncidentsResponse(
        items,
        pageResult.page(),
        pageResult.size(),
        pageResult.totalElements(),
        pageResult.totalPages());
  }
}
