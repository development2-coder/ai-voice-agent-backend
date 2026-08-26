package com.infinitio.aivoiceplatform.flow.service.impl;

import com.infinitio.aivoiceplatform.flow.entity.FlowEdge;
import com.infinitio.aivoiceplatform.flow.service.FlowConditionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FlowConditionServiceImpl
        implements FlowConditionService {

    @Override
    public FlowEdge findMatchingEdge(
            List<FlowEdge> edges,
            Map<String, Object> context) {

        if (edges == null || edges.isEmpty()) {
            return null;
        }

        if (context == null) {
            context = Map.of();
        }

        /*
         * Edges are expected to already be ordered
         * according to their priority.
         */
        FlowEdge defaultEdge = null;

        for (FlowEdge edge : edges) {

            if (edge == null) {
                continue;
            }

            String expression =
                    edge.getConditionExpression();

            /*
             * An edge without a condition is treated
             * as the default/fallback edge.
             */
            if (expression == null
                    || expression.isBlank()) {

                if (defaultEdge == null) {
                    defaultEdge = edge;
                }

                continue;
            }

            try {

                if (evaluate(
                        expression,
                        context
                )) {

                    log.debug(
                            "Flow condition matched. expression={}",
                            expression
                    );

                    return edge;
                }

            } catch (Exception exception) {

                log.warn(
                        "Unable to evaluate flow condition. expression={}",
                        expression,
                        exception
                );
            }
        }

        /*
         * If no conditional edge matched,
         * use the default edge if available.
         */
        if (defaultEdge != null) {

            log.debug(
                    "Using default flow transition."
            );

            return defaultEdge;
        }

        return null;
    }

    @Override
    public boolean evaluate(
            String expression,
            Map<String, Object> context) {

        if (expression == null
                || expression.isBlank()) {

            return false;
        }

        if (context == null) {
            context = Map.of();
        }

        String condition =
                expression.trim();

        /*
         * Check operators in this order.
         *
         * >= and <= must be checked before
         * > and <.
         */
        if (condition.contains("==")) {

            return evaluateEquality(
                    condition,
                    "==",
                    context
            );
        }

        if (condition.contains("!=")) {

            return evaluateEquality(
                    condition,
                    "!=",
                    context
            );
        }

        if (condition.contains(">=")) {

            return evaluateNumeric(
                    condition,
                    ">=",
                    context
            );
        }

        if (condition.contains("<=")) {

            return evaluateNumeric(
                    condition,
                    "<=",
                    context
            );
        }

        if (condition.contains(">")) {

            return evaluateNumeric(
                    condition,
                    ">",
                    context
            );
        }

        if (condition.contains("<")) {

            return evaluateNumeric(
                    condition,
                    "<",
                    context
            );
        }

        log.warn(
                "Unsupported flow condition: {}",
                expression
        );

        return false;
    }

    private boolean evaluateEquality(
            String expression,
            String operator,
            Map<String, Object> context) {

        String[] parts =
                splitExpression(
                        expression,
                        operator
                );

        if (parts == null) {
            return false;
        }

        String variable =
                parts[0].trim();

        String expectedValue =
                cleanValue(
                        parts[1].trim()
                );

        Object actualValue =
                context.get(variable);

        if (actualValue == null) {
            return false;
        }

        boolean equals =
                String.valueOf(actualValue)
                        .equalsIgnoreCase(
                                expectedValue
                        );

        if ("==".equals(operator)) {
            return equals;
        }

        return !equals;
    }

    private boolean evaluateNumeric(
            String expression,
            String operator,
            Map<String, Object> context) {

        String[] parts =
                splitExpression(
                        expression,
                        operator
                );

        if (parts == null) {
            return false;
        }

        String variable =
                parts[0].trim();

        String expectedValue =
                cleanValue(
                        parts[1].trim()
                );

        Object actualObject =
                context.get(variable);

        if (actualObject == null) {
            return false;
        }

        try {

            double actual =
                    Double.parseDouble(
                            String.valueOf(
                                    actualObject
                            )
                    );

            double expected =
                    Double.parseDouble(
                            expectedValue
                    );

            return switch (operator) {

                case ">" ->
                        actual > expected;

                case "<" ->
                        actual < expected;

                case ">=" ->
                        actual >= expected;

                case "<=" ->
                        actual <= expected;

                default ->
                        false;
            };

        } catch (NumberFormatException exception) {

            log.warn(
                    "Non-numeric value used in numeric condition. expression={}",
                    expression
            );

            return false;
        }
    }

    private String[] splitExpression(
            String expression,
            String operator) {

        String[] parts =
                expression.split(
                        java.util.regex.Pattern
                                .quote(operator),
                        2
                );

        if (parts.length != 2) {

            log.warn(
                    "Invalid flow condition: {}",
                    expression
            );

            return null;
        }

        if (parts[0].isBlank()
                || parts[1].isBlank()) {

            log.warn(
                    "Incomplete flow condition: {}",
                    expression
            );

            return null;
        }

        return parts;
    }

    private String cleanValue(
            String value) {

        String cleaned =
                value.trim();

        /*
         * Remove surrounding single or
         * double quotes.
         */
        if (cleaned.length() >= 2) {

            boolean doubleQuoted =
                    cleaned.startsWith("\"")
                            && cleaned.endsWith("\"");

            boolean singleQuoted =
                    cleaned.startsWith("'")
                            && cleaned.endsWith("'");

            if (doubleQuoted || singleQuoted) {

                return cleaned.substring(
                        1,
                        cleaned.length() - 1
                );
            }
        }

        return cleaned;
    }
}