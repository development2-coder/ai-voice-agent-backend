package com.infinitio.aivoiceplatform.flow.service;

import java.util.Map;

/**
 * Provides runtime HTTP execution for Flow API nodes.
 *
 * <p>
 * The service abstracts HTTP communication from the Flow node
 * execution layer. API nodes provide the request details and the
 * runtime service performs the actual HTTP operation.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface ApiRuntimeService {

    /**
     * Executes an HTTP API request.
     *
     * @param url request URL
     * @param method HTTP method
     * @param headers request headers
     * @param body request body
     * @return API response body
     */
    Object execute(
            String url,
            String method,
            Map<String, Object> headers,
            Object body
    );
}