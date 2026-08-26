package com.infinitio.aivoiceplatform.runtime.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.runtime.context.RuntimeContext;
import com.infinitio.aivoiceplatform.runtime.service.RuntimeContextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * Default implementation of the runtime context service.
 *
 * <p>
 * Converts runtime context to and from JSON so that the context
 * can be persisted in the existing flow execution context data.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuntimeContextServiceImpl
        implements RuntimeContextService {

    /**
     * Jackson object mapper used for runtime context
     * serialization and deserialization.
     */
    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(
            RuntimeContext context) {

        if (context == null) {

            return "{}";
        }

        try {

            return objectMapper.writeValueAsString(
                    context
            );

        } catch (JsonProcessingException exception) {

            log.error(
                    "Unable to serialize runtime context.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to serialize runtime context.",
                    exception
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RuntimeContext deserialize(
            String contextData) {

        if (contextData == null
                || contextData.isBlank()) {

            return RuntimeContext.builder()
                    .build();
        }

        try {

            return objectMapper.readValue(
                    contextData,
                    RuntimeContext.class
            );

        } catch (JsonProcessingException exception) {

            log.error(
                    "Unable to deserialize runtime context.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to deserialize runtime context.",
                    exception
            );
        }
    }
}