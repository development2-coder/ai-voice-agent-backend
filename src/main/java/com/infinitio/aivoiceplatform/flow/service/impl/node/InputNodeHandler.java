package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InputNodeHandler implements FlowNodeHandler {

    private static final String MESSAGE_KEY = "message";

    private static final String VARIABLE_KEY = "variable";

    private static final String WAITING_VARIABLE_KEY =
            "_waitingVariable";

    private final ObjectMapper objectMapper;

    @Override
    public FlowNodeType getNodeType() {
        return FlowNodeType.USER_INPUT;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing input node. execution={}, node={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> nodeConfiguration =
                readConfiguration(
                        node.getConfiguration()
                );

        String message =
                getStringValue(
                        nodeConfiguration,
                        MESSAGE_KEY
                );

        String variable =
                getStringValue(
                        nodeConfiguration,
                        VARIABLE_KEY
                );

        if (variable == null
                || variable.isBlank()) {

            throw new IllegalArgumentException(
                    "Input node variable is required."
            );
        }

        /*
         * Store the variable name so that when the
         * user responds, FlowExecutionServiceImpl
         * knows where to store the response.
         */
        context.put(
                WAITING_VARIABLE_KEY,
                variable
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.WAITING_FOR_INPUT
                )
                .action(
                        "WAIT_FOR_INPUT"
                )
                .outputText(
                        message
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
                    "Invalid INPUT node configuration. configuration={}",
                    configuration,
                    exception
            );

            throw new IllegalArgumentException(
                    "Invalid INPUT node configuration.",
                    exception
            );
        }
    }

    private String getStringValue(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(key);

        return value == null
                ? null
                : String.valueOf(value).trim();
    }
}