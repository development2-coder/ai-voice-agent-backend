package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
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

    private static final String PROMPT_KEY =
            "prompt";

    private static final String VARIABLE_KEY =
            "variable";

    private static final String OUTPUT_VARIABLE_KEY =
            "outputVariable";

    private static final String LLM_CONFIG_PUBLIC_ID_KEY =
            "llmConfigPublicId";

    private static final String TEMPERATURE_KEY =
            "temperature";

    private static final String WAITING_AI_VARIABLE =
            FlowExecutionContextKeys.WAITING_AI_VARIABLE;

    private static final String AI_PROMPT =
            FlowExecutionContextKeys.AI_PROMPT;

    private static final String AI_LLM_CONFIG_PUBLIC_ID =
            "llmConfigPublicId";

    private static final String AI_TEMPERATURE =
            "aiTemperature";

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

        if (execution == null) {

            throw new IllegalArgumentException(
                    "Flow execution cannot be null."
            );
        }

        if (node == null) {

            throw new IllegalArgumentException(
                    "Flow node cannot be null."
            );
        }

        if (context == null) {

            throw new IllegalArgumentException(
                    "Flow execution context cannot be null."
            );
        }

        log.info(
                "Executing AI_RESPONSE node. " +
                        "executionPublicId={}, nodeKey={}",
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

        String resolvedPrompt =
                flowContextService.replaceVariables(
                        prompt,
                        context
                );

        /*
         * Store the resolved prompt for ConversationAiService.
         */
        context.put(
                AI_PROMPT,
                resolvedPrompt
        );

        /*
         * Preserve the LLM configuration selected by
         * the Flow Builder.
         */
        String llmConfigPublicId =
                getOptionalValue(
                        configuration,
                        LLM_CONFIG_PUBLIC_ID_KEY
                );

        if (llmConfigPublicId != null
                && !llmConfigPublicId.isBlank()) {

            context.put(
                    AI_LLM_CONFIG_PUBLIC_ID,
                    llmConfigPublicId
            );
        }

        /*
         * Preserve temperature configured by the Flow Builder.
         */
        Object temperature =
                configuration.get(
                        TEMPERATURE_KEY
                );

        if (temperature != null) {

            context.put(
                    AI_TEMPERATURE,
                    temperature
            );
        }

        /*
         * The frontend uses outputVariable.
         *
         * The older backend used variable.
         * Support both so existing flows remain compatible.
         */
        String outputVariable =
                getOptionalValue(
                        configuration,
                        OUTPUT_VARIABLE_KEY
                );

        if (outputVariable == null
                || outputVariable.isBlank()) {

            outputVariable =
                    getOptionalValue(
                            configuration,
                            VARIABLE_KEY
                    );
        }

        if (outputVariable != null
                && !outputVariable.isBlank()) {

            context.put(
                    WAITING_AI_VARIABLE,
                    outputVariable
            );
        }

        log.info(
                "AI_RESPONSE node is waiting for AI processing. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "llmConfigPresent={}, outputVariable={}",
                execution.getPublicId(),
                node.getNodeKey(),
                llmConfigPublicId != null
                        && !llmConfigPublicId.isBlank(),
                outputVariable
        );

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