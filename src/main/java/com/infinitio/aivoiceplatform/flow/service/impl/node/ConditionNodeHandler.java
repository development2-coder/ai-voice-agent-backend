package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionContextKeys;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowConditionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Handles CONDITION flow nodes.
 *
 * <p>
 * The configured expression is evaluated against the current
 * flow execution context. The evaluation result determines
 * whether the {@code true} or {@code false} output port is
 * selected.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionNodeHandler
        implements FlowNodeHandler {

    private static final String EXPRESSION =
            "expression";

    private static final String TRUE_PORT =
            "true";

    private static final String FALSE_PORT =
            "false";

    private static final String ACTION_EVALUATE_CONDITION =
            "EVALUATE_CONDITION";

    private final ObjectMapper objectMapper;

    private final FlowConditionService flowConditionService;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.CONDITION;
    }

    /**
     * Evaluates the configured condition and selects
     * the appropriate output port.
     *
     * @param execution current flow execution
     * @param node condition node
     * @param context current flow execution context
     * @return flow node execution result
     */
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
                "Executing condition node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> configuration =
                parseConfiguration(
                        node.getConfiguration()
                );

        String expression =
                resolveExpression(
                        configuration
                );

        boolean result =
                flowConditionService.evaluate(
                        expression,
                        context
                );

        String selectedOutputPort =
                result
                        ? TRUE_PORT
                        : FALSE_PORT;

        context.put(
                FlowExecutionContextKeys.SELECTED_OUTPUT_PORT,
                selectedOutputPort
        );

        log.info(
                "Condition evaluated. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "expression={}, result={}, selectedOutputPort={}",
                execution.getPublicId(),
                node.getNodeKey(),
                expression,
                result,
                selectedOutputPort
        );

        return FlowNodeExecutionResult.builder()
                .action(
                        ACTION_EVALUATE_CONDITION
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(context)
                .build();
    }

    /**
     * Parses the condition node configuration.
     *
     * @param configurationJson node configuration JSON
     * @return parsed configuration
     */
    private Map<String, Object> parseConfiguration(
            String configurationJson) {

        if (configurationJson == null
                || configurationJson.isBlank()) {

            return Collections.emptyMap();
        }

        try {

            return objectMapper.readValue(
                    configurationJson,
                    new TypeReference<Map<String, Object>>() {
                    }
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to parse CONDITION node configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    "Invalid CONDITION node configuration.",
                    exception
            );
        }
    }

    /**
     * Resolves the condition expression from node configuration.
     *
     * @param configuration parsed node configuration
     * @return condition expression
     */
    private String resolveExpression(
            Map<String, Object> configuration) {

        Object expression =
                configuration.get(
                        EXPRESSION
                );

        if (expression == null
                || String.valueOf(expression).isBlank()) {

            throw new IllegalArgumentException(
                    "CONDITION node requires an expression."
            );
        }

        return String.valueOf(
                expression
        ).trim();
    }
}