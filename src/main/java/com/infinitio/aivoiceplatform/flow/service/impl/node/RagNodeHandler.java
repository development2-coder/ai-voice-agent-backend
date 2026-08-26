package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagNodeHandler
        implements FlowNodeHandler {

    private static final String QUERY_KEY = "query";

    private static final String VARIABLE_KEY = "variable";

    private static final String RAG_QUERY_KEY =
            "_ragQuery";

    private static final String RAG_VARIABLE_KEY =
            "_ragVariable";

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.RAG;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing RAG node. execution={}, node={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String query =
                getRequiredValue(
                        configuration,
                        QUERY_KEY
                );

        String variable =
                getOptionalValue(
                        configuration,
                        VARIABLE_KEY
                );

        /*
         * Resolve flow variables.
         *
         * Example:
         *
         * "What is the status of {{customerId}}?"
         */
        String resolvedQuery =
                flowContextService.replaceVariables(
                        query,
                        context
                );

        /*
         * Store the RAG request in runtime context.
         *
         * The actual RAG/vector search will be handled
         * by the RAG service later.
         */
        context.put(
                RAG_QUERY_KEY,
                resolvedQuery
        );

        if (variable != null
                && !variable.isBlank()) {

            context.put(
                    RAG_VARIABLE_KEY,
                    variable
            );
        }

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.WAITING_FOR_AI
                )
                .action(
                        "RAG_SEARCH_REQUIRED"
                )
                .waiting(true)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    private Map<String, Object> readConfiguration(
            String configuration) {

        if (configuration == null
                || configuration.isBlank()) {

            return new HashMap<>();
        }

        try {

            return objectMapper.readValue(
                    configuration,
                    Map.class
            );

        } catch (Exception exception) {

            log.error(
                    "Invalid RAG node configuration. configuration={}",
                    configuration,
                    exception
            );

            throw new IllegalArgumentException(
                    "Invalid RAG node configuration.",
                    exception
            );
        }
    }

    private String getRequiredValue(
            Map<String, Object> configuration,
            String key) {

        String value =
                getOptionalValue(
                        configuration,
                        key
                );

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    "RAG node requires: " + key
            );
        }

        return value;
    }

    private String getOptionalValue(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(key);

        return value == null
                ? null
                : String.valueOf(value).trim();
    }
}