package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class MessageNodeHandler implements FlowNodeHandler {

    private static final String MESSAGE_KEY = "message";

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    @Override
    public FlowNodeType getNodeType() {
        return FlowNodeType.MESSAGE;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing message node. execution={}, node={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String message =
                getMessage(configuration);

        /*
         * Replace runtime variables.
         *
         * Example:
         *
         * Hello {{customerName}}
         *
         * becomes:
         *
         * Hello Kiran
         */
        String resolvedMessage =
                flowContextService.replaceVariables(
                        message,
                        context
                );

        return FlowNodeExecutionResult.builder()
                .action("SPEAK")
                .outputText(resolvedMessage)
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    private String getMessage(
            Map<String, Object> configuration) {

        Object value =
                configuration.get(
                        MESSAGE_KEY
                );

        if (value == null
                || String.valueOf(value)
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Message node requires a message."
            );
        }

        return String.valueOf(value);
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
                    "Invalid MESSAGE node configuration. configuration={}",
                    configuration,
                    exception
            );

            throw new IllegalArgumentException(
                    "Invalid MESSAGE node configuration.",
                    exception
            );
        }
    }
}