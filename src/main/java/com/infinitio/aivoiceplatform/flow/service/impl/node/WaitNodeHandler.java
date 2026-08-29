package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handler for WAIT nodes.
 *
 * <p>
 * The WAIT node pauses Flow execution for the configured
 * duration. The execution is persisted with a resume timestamp
 * so that the timer can later be resumed by the runtime layer.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 *
 * <pre>
 * {
 *   "durationSeconds": 30
 * }
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WaitNodeHandler
        implements FlowNodeHandler {

    private static final String DURATION_SECONDS =
            "durationSeconds";

    private static final String ACTION =
            "WAIT";

    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.WAIT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        log.info(
                "Executing WAIT node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        long durationSeconds =
                readDuration(
                        configuration
                );

        LocalDateTime resumeAt =
                LocalDateTime.now()
                        .plusSeconds(
                                durationSeconds
                        );

        context.put(
                FlowExecutionContextKeys.WAIT_RESUME_AT,
                resumeAt.toString()
        );

        context.put(
                FlowExecutionContextKeys.WAIT_DURATION_SECONDS,
                durationSeconds
        );

        context.put(
                FlowExecutionContextKeys.WAIT_NODE_PUBLIC_ID,
                node.getPublicId()
        );

        execution.setStatus(
                FlowExecutionStatus.WAITING_FOR_TIMER
        );

        log.info(
                "Flow execution paused by WAIT node. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "durationSeconds={}, resumeAt={}",
                execution.getPublicId(),
                node.getNodeKey(),
                durationSeconds,
                resumeAt
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.WAITING_FOR_TIMER
                )
                .action(
                        ACTION
                )
                .waiting(true)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    // =========================================================
    // CONFIGURATION
    // =========================================================

    private Map<String, Object> readConfiguration(
            String configuration) {

        if (configuration == null
                || configuration.isBlank()) {

            log.warn(
                    "WAIT node configuration is empty."
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        try {

            return objectMapper.readValue(
                    configuration,
                    Map.class
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to parse WAIT node configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    private long readDuration(
            Map<String, Object> configuration) {

        Object value =
                configuration.get(
                        DURATION_SECONDS
                );

        if (value == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        long durationSeconds;

        try {

            durationSeconds =
                    Long.parseLong(
                            String.valueOf(
                                    value
                            )
                    );

        } catch (NumberFormatException exception) {

            log.warn(
                    "Invalid WAIT duration. value={}",
                    value
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }

        if (durationSeconds <= 0) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return durationSeconds;
    }
}