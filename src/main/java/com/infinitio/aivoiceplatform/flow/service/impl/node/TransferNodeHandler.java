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
public class TransferNodeHandler
        implements FlowNodeHandler {

    private static final String DESTINATION_KEY =
            "destination";

    private static final String MESSAGE_KEY =
            "message";

    private static final String TRANSFER_DESTINATION_KEY =
            "_transferDestination";

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.TRANSFER;
    }

    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing TRANSFER node. execution={}, node={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String destination =
                getRequiredValue(
                        configuration,
                        DESTINATION_KEY
                );

        String message =
                getOptionalValue(
                        configuration,
                        MESSAGE_KEY
                );

        /*
         * Resolve variables from the flow context.
         */
        destination =
                flowContextService.replaceVariables(
                        destination,
                        context
                );

        if (message != null
                && !message.isBlank()) {

            message =
                    flowContextService.replaceVariables(
                            message,
                            context
                    );
        }

        /*
         * Store transfer information in the
         * execution context.
         *
         * The actual Exotel/human-agent transfer
         * will be handled by the call/integration
         * layer.
         */
        context.put(
                TRANSFER_DESTINATION_KEY,
                destination
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.TRANSFERRED
                )
                .action(
                        "TRANSFER"
                )
                .outputText(
                        message
                )
                .waiting(false)
                .completed(false)
                .transferred(true)
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
                    "Invalid TRANSFER node configuration. configuration={}",
                    configuration,
                    exception
            );

            throw new IllegalArgumentException(
                    "Invalid TRANSFER node configuration.",
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
                    "TRANSFER node requires: " + key
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