package com.infinitio.aivoiceplatform.common.filter;

import com.infinitio.aivoiceplatform.common.util.RequestIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Generates a unique Request ID for every incoming request.
 *
 * The Request ID is stored in MDC so that all logs generated during the
 * request lifecycle can be correlated.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String requestId = RequestIdUtil.generateRequestId();

        RequestIdUtil.setRequestId(requestId);

        response.setHeader(REQUEST_ID_HEADER, requestId);

        log.info("========================================================");
        log.info("Incoming Request");
        log.info("Request Id : {}", requestId);
        log.info("HTTP Method: {}", request.getMethod());
        log.info("URI        : {}", request.getRequestURI());
        log.info("Client IP  : {}", request.getRemoteAddr());

        try {

            filterChain.doFilter(request, response);

        } finally {

            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Outgoing Response");
            log.info("Request Id    : {}", requestId);
            log.info("Status Code   : {}", response.getStatus());
            log.info("Execution Time: {} ms", executionTime);
            log.info("========================================================");

            RequestIdUtil.clear();
        }
    }

}