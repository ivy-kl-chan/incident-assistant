package com.incidentassistant.web.incident;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Echoes incoming {@value #REQUEST_ID_HEADER} or generates one for correlation ({@code
 * api-contract.md}).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-Id";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(REQUEST_ID_HEADER);
    String requestId =
        header != null && !header.isBlank() ? header.trim() : UUID.randomUUID().toString();
    response.setHeader(REQUEST_ID_HEADER, requestId);
    filterChain.doFilter(request, response);
  }
}
