package com.incidentassistant.web.incident;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

  private final RequestIdFilter filter = new RequestIdFilter();

  @Test
  void echoesProvidedRequestId() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "  req-123  ");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("req-123");
  }

  @Test
  void generatesRequestIdWhenHeaderMissingOrBlank() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isNotBlank();

    request = new MockHttpServletRequest();
    request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "   ");
    response = new MockHttpServletResponse();
    filter.doFilterInternal(request, response, (req, res) -> {});

    assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isNotBlank();
  }
}
