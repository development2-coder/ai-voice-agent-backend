package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handler for FUNCTION nodes.
 *
 * <p>
 * FUNCTION nodes execute controlled expressions against the
 * current Flow execution context.
 * </p>
 *
 * <p>
 * Arbitrary Java source code is intentionally not compiled or
 * executed by this handler. Function execution is restricted to
 * predefined operations to prevent arbitrary server-side code
 * execution.
 * </p>
 *
 * <p>
 * Supported examples:
 * </p>
 *
 * <pre>
 * UPPERCASE(customerName)
 * LOWERCASE(customerName)
 * TRIM(customerName)
 * LENGTH(customerName)
 * TO_STRING(customerId)
 * TO_NUMBER(amount)
 * CONCAT(firstName, ' ', lastName)
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FunctionNodeHandler
        implements FlowNodeHandler {

    private static final String CODE_KEY =
            "code";

    private static final String ACTION =
            "FUNCTION";

    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.FUNCTION;
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
                "Executing FUNCTION node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        validateContext(
                context
        );

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String code =
                readCode(
                        configuration
                );

        Object result =
                executeFunction(
                        code,
                        context
                );

        /*
         * Store the result using a predictable context key.
         * This allows the following node to consume the value.
         */
        context.put(
                "lastFunctionResult",
                result
        );

        log.info(
                "FUNCTION node completed. " +
                        "executionPublicId={}, nodeKey={}, resultType={}",
                execution.getPublicId(),
                node.getNodeKey(),
                result == null
                        ? null
                        : result.getClass().getSimpleName()
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .action(
                        ACTION
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .outputText(
                        result == null
                                ? null
                                : String.valueOf(result)
                )
                .context(
                        context
                )
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
                    "FUNCTION node configuration is empty."
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        try {

            return objectMapper.readValue(
                    configuration,
                    new TypeReference<
                            Map<String, Object>>() {
                    }
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to parse FUNCTION configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    private String readCode(
            Map<String, Object> configuration) {

        Object value =
                configuration.get(
                        CODE_KEY
                );

        if (value == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        String code =
                String.valueOf(
                        value
                ).trim();

        if (code.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return code;
    }

    // =========================================================
    // FUNCTION ENGINE
    // =========================================================

    private Object executeFunction(
            String code,
            Map<String, Object> context) {

        String expression =
                code.trim();

        log.debug(
                "Executing controlled Flow function. expression={}",
                expression
        );

        int openingBracket =
                expression.indexOf('(');

        int closingBracket =
                expression.lastIndexOf(')');

        if (openingBracket <= 0
                || closingBracket <= openingBracket
                || closingBracket != expression.length() - 1) {

            throw new IllegalArgumentException(
                    "Invalid FUNCTION expression."
            );
        }

        String operation =
                expression
                        .substring(
                                0,
                                openingBracket
                        )
                        .trim()
                        .toUpperCase();

        String argumentExpression =
                expression.substring(
                        openingBracket + 1,
                        closingBracket
                );

        List<String> arguments =
                parseArguments(
                        argumentExpression
                );

        return switch (operation) {

            case "UPPERCASE" ->
                    uppercase(
                            requireArgument(
                                    arguments,
                                    0
                            ),
                            context
                    );

            case "LOWERCASE" ->
                    lowercase(
                            requireArgument(
                                    arguments,
                                    0
                            ),
                            context
                    );

            case "TRIM" ->
                    trim(
                            requireArgument(
                                    arguments,
                                    0
                            ),
                            context
                    );

            case "LENGTH" ->
                    length(
                            requireArgument(
                                    arguments,
                                    0
                            ),
                            context
                    );

            case "TO_STRING" ->
                    toStringValue(
                            requireArgument(
                                    arguments,
                                    0
                            ),
                            context
                    );

            case "TO_NUMBER" ->
                    toNumber(
                            requireArgument(
                                    arguments,
                                    0
                            ),
                            context
                    );

            case "CONCAT" ->
                    concat(
                            arguments,
                            context
                    );

            default -> {

                log.warn(
                        "Unsupported FUNCTION operation. operation={}",
                        operation
                );

                throw new IllegalArgumentException(
                        "Unsupported FUNCTION operation: "
                                + operation
                );
            }
        };
    }

    // =========================================================
    // OPERATIONS
    // =========================================================

    private String uppercase(
            String argument,
            Map<String, Object> context) {

        Object value =
                resolveArgument(
                        argument,
                        context
                );

        return value == null
                ? null
                : String.valueOf(
                value
        ).toUpperCase();
    }

    private String lowercase(
            String argument,
            Map<String, Object> context) {

        Object value =
                resolveArgument(
                        argument,
                        context
                );

        return value == null
                ? null
                : String.valueOf(
                value
        ).toLowerCase();
    }

    private String trim(
            String argument,
            Map<String, Object> context) {

        Object value =
                resolveArgument(
                        argument,
                        context
                );

        return value == null
                ? null
                : String.valueOf(
                value
        ).trim();
    }

    private int length(
            String argument,
            Map<String, Object> context) {

        Object value =
                resolveArgument(
                        argument,
                        context
                );

        return value == null
                ? 0
                : String.valueOf(
                value
        ).length();
    }

    private String toStringValue(
            String argument,
            Map<String, Object> context) {

        Object value =
                resolveArgument(
                        argument,
                        context
                );

        return value == null
                ? null
                : String.valueOf(
                value
        );
    }

    private Number toNumber(
            String argument,
            Map<String, Object> context) {

        Object value =
                resolveArgument(
                        argument,
                        context
                );

        if (value == null) {
            return null;
        }

        String stringValue =
                String.valueOf(
                        value
                ).trim();

        try {

            if (stringValue.contains(".")) {

                return Double.parseDouble(
                        stringValue
                );
            }

            return Long.parseLong(
                    stringValue
            );

        } catch (NumberFormatException exception) {

            log.warn(
                    "Unable to convert FUNCTION value to number. " +
                            "value={}",
                    stringValue
            );

            throw new IllegalArgumentException(
                    "FUNCTION value cannot be converted to number.",
                    exception
            );
        }
    }

    private String concat(
            List<String> arguments,
            Map<String, Object> context) {

        StringBuilder result =
                new StringBuilder();

        for (String argument : arguments) {

            Object value =
                    resolveArgument(
                            argument,
                            context
                    );

            if (value != null) {
                result.append(
                        value
                );
            }
        }

        return result.toString();
    }

    // =========================================================
    // ARGUMENTS
    // =========================================================

    private List<String> parseArguments(
            String expression) {

        List<String> arguments =
                new ArrayList<>();

        if (expression == null
                || expression.isBlank()) {

            return arguments;
        }

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        char quoteCharacter = 0;

        for (int index = 0;
             index < expression.length();
             index++) {

            char character =
                    expression.charAt(
                            index
                    );

            if ((character == '\''
                    || character == '"')) {

                if (!insideQuotes) {

                    insideQuotes = true;
                    quoteCharacter = character;

                } else if (quoteCharacter == character) {

                    insideQuotes = false;
                }

                current.append(
                        character
                );

                continue;
            }

            if (character == ','
                    && !insideQuotes) {

                arguments.add(
                        current.toString().trim()
                );

                current.setLength(
                        0
                );

                continue;
            }

            current.append(
                    character
            );
        }

        if (current.length() > 0) {

            arguments.add(
                    current.toString().trim()
            );
        }

        return arguments;
    }

    private String requireArgument(
            List<String> arguments,
            int index) {

        if (arguments.size() <= index) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        String argument =
                arguments.get(
                        index
                );

        if (argument == null
                || argument.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return argument;
    }

    private Object resolveArgument(
            String argument,
            Map<String, Object> context) {

        String value =
                argument.trim();

        if (isQuoted(
                value
        )) {

            return value.substring(
                    1,
                    value.length() - 1
            );
        }

        if (context.containsKey(
                value
        )) {

            return context.get(
                    value
            );
        }

        /*
         * Support {{variable}} syntax as well.
         */
        if (value.startsWith("{{")
                && value.endsWith("}}")) {

            String variableName =
                    value.substring(
                            2,
                            value.length() - 2
                    ).trim();

            return context.get(
                    variableName
            );
        }

        /*
         * Treat numeric literals as values.
         */
        try {

            if (value.contains(".")) {

                return Double.parseDouble(
                        value
                );
            }

            return Long.parseLong(
                    value
            );

        } catch (NumberFormatException ignored) {
            // Continue as a literal string.
        }

        return value;
    }

    private boolean isQuoted(
            String value) {

        return value.length() >= 2
                && (
                value.startsWith("'")
                        && value.endsWith("'")
                        || value.startsWith("\"")
                        && value.endsWith("\"")
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateContext(
            Map<String, Object> context) {

        if (context != null) {
            return;
        }

        log.error(
                "FUNCTION execution context is null."
        );

        throw new IllegalArgumentException(
                FlowMessages.EXECUTION_FAILED
        );
    }
}