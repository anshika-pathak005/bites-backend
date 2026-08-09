package com.bites.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestResponseLoggingFilter();
        MDC.clear();
    }

    @Test
    void shouldGenerateRequestIdWhenHeaderNotPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/menu");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            mdcValueInsideChain.set(MDC.get(RequestResponseLoggingFilter.MDC_REQUEST_ID_KEY));
            ((HttpServletResponse) res).setStatus(200);
        };

        filter.doFilterInternal(request, response, chain);

        assertNotNull(mdcValueInsideChain.get(), "MDC should contain requestId during chain execution");
        assertFalse(mdcValueInsideChain.get().isBlank());
        assertEquals(mdcValueInsideChain.get(), response.getHeader(RequestResponseLoggingFilter.REQUEST_ID_HEADER));
        assertNull(MDC.get(RequestResponseLoggingFilter.MDC_REQUEST_ID_KEY), "MDC should be cleared after filter completion");
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldPreserveValidIncomingRequestId() throws ServletException, IOException {
        String customId = "custom-client-req-12345";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders");
        request.addHeader(RequestResponseLoggingFilter.REQUEST_ID_HEADER, customId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValueInsideChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            mdcValueInsideChain.set(MDC.get(RequestResponseLoggingFilter.MDC_REQUEST_ID_KEY));
            ((HttpServletResponse) res).setStatus(201);
        };

        filter.doFilterInternal(request, response, chain);

        assertEquals(customId, mdcValueInsideChain.get());
        assertEquals(customId, response.getHeader(RequestResponseLoggingFilter.REQUEST_ID_HEADER));
        assertNull(MDC.get(RequestResponseLoggingFilter.MDC_REQUEST_ID_KEY));
        assertEquals(201, response.getStatus());
    }

    @Test
    void shouldSanitizeSensitiveQueryParams() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/search");
        request.setQueryString("query=pizza&token=secret123&password=myPassword&safeParam=value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            ((HttpServletResponse) res).setStatus(200);
        };

        filter.doFilterInternal(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNull(MDC.get(RequestResponseLoggingFilter.MDC_REQUEST_ID_KEY));
    }

    @Test
    void shouldHandleChainExceptionAndStillClearMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/error");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            throw new RuntimeException("Simulated error in filter chain");
        };

        assertThrows(RuntimeException.class, () -> filter.doFilterInternal(request, response, chain));
        assertNull(MDC.get(RequestResponseLoggingFilter.MDC_REQUEST_ID_KEY), "MDC must be cleared even when an exception occurs");
    }
}
