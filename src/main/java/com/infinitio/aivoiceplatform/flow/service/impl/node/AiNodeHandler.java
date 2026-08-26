package com.infinitio.aivoiceplatform.flow.service.impl.node;

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
public class AiNodeHandler implements FlowNodeHandler {

    private static final String PROMPT_KEY = "prompt";

    private static final String VARIABLE_KEY = "variable";

    private static final String WAITING_AI_VARIABLE =
            "_waitingAiVariable";

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    @Override
    public FlowNodeType getNodeType() {
        return FlowNodeType.AI_RESPONSE;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing AI node. execution={}, node={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String prompt =
                getRequiredValue(
                        configuration,
                        PROMPT_KEY
                );

        String outputVariable =
                getOptionalValue(
                        configuration,
                        VARIABLE_KEY
                );

        /*
         * Replace flow variables in the prompt.
         *
         * Example:
         *
         * "Help {{customerName}} with {{lastUserInput}}"
         */
        String resolvedPrompt =
                flowContextService.replaceVariables(
                        prompt,
                        context
                );

        /*
         * Store information required by the
         * future AI/LLM integration layer.
         *
         * We are NOT calling Sarvam here.
         */
        context.put(
                "_aiPrompt",
                resolvedPrompt
        );

        if (outputVariable != null
                && !outputVariable.isBlank()) {

            context.put(
                    WAITING_AI_VARIABLE,
                    outputVariable
            );
        }

        /*
         * AI execution is asynchronous from the
         * flow engine's point of view.
         *
         * The Conversation/AI integration layer
         * will eventually provide the response.
         */
        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.WAITING_FOR_AI
                )
                .action(
                        "WAIT_FOR_AI"
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
                    "Invalid AI node configuration. configuration={}",
                    configuration,
                    exception
            );

            throw new IllegalArgumentException(
                    "Invalid AI node configuration.",
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
                    "AI node requires: " + key
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