package com.infinitio.aivoiceplatform.flow.service.impl.node;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes Flow TRANSFER nodes.
 *
 * <p>
 * The handler prepares a provider-neutral transfer instruction.
 * Actual telephony transfer execution is handled outside the
 * Flow module.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransferNodeHandler
        implements FlowNodeHandler {

    private static final String DESTINATION =
            "destination";

    private static final String MESSAGE =
            "message";

    private static final String ACTION =
            "TRANSFER";

    private final ObjectMapper objectMapper;

    private final FlowContextService flowContextService;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing TRANSFER node. executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                parseConfiguration(
                        node.getConfiguration()
                );

        String destination =
                resolveString(
                        configuration,
                        DESTINATION,
                        context
                );

        if (destination == null
                || destination.isBlank()) {

            log.warn(
                    "Transfer destination is missing. nodeKey={}",
                    node.getNodeKey()
            );

            throw new IllegalArgumentException(
                    FlowMessages.TRANSFER_DESTINATION_REQUIRED
            );
        }

        String message =
                resolveString(
                        configuration,
                        MESSAGE,
                        context
                );

        context.put(
                FlowExecutionContextKeys.TRANSFER_DESTINATION,
                destination
        );

        if (message != null
                && !message.isBlank()) {

            context.put(
                    FlowExecutionContextKeys.TRANSFER_MESSAGE,
                    message
            );
        }

        log.info(
                "Transfer instruction prepared. " +
                        "executionPublicId={}, nodeKey={}, destination={}",
                execution.getPublicId(),
                node.getNodeKey(),
                destination
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.TRANSFERRED
                )
                .action(ACTION)
                .outputText(message)
                .waiting(false)
                .completed(false)
                .transferred(true)
                .context(context)
                .build();
    }

    /**
     * Parses node configuration.
     *
     * @param configuration JSON configuration
     * @return configuration map
     */
    private Map<String, Object> parseConfiguration(
            String configuration) {

        try {

            if (configuration == null
                    || configuration.isBlank()) {

                return Map.of();
            }

            return objectMapper.readValue(
                    configuration,
                    new TypeReference<Map<String, Object>>() {
                    }
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to parse TRANSFER node configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Resolves a configuration value and supports Flow
     * context expressions.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @param context runtime context
     * @return resolved value
     */
    private String resolveString(
            Map<String, Object> configuration,
            String key,
            Map<String, Object> context) {

        Object value =
                configuration.get(key);

        if (value == null) {
            return null;
        }

        return flowContextService.replaceVariables(
                value.toString(),
                context
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.TRANSFER;
    }
}