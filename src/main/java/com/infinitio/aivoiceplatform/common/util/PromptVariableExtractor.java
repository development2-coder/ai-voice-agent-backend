package com.infinitio.aivoiceplatform.common.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for extracting dynamic variables from prompt text.
 *
 * <p>
 * Prompt variables must use the following format:
 * {@code {{variableName}}}.
 * </p>
 *
 * <p>
 * Duplicate variables are removed while preserving their
 * first-occurrence order.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class PromptVariableExtractor {

    /**
     * Pattern used to identify prompt variables.
     */
    private static final Pattern VARIABLE_PATTERN =
            Pattern.compile(
                    "\\{\\{\\s*([^{}]+?)\\s*\\}\\}"
            );

    /**
     * Private constructor to prevent instantiation.
     */
    private PromptVariableExtractor() {
    }

    /**
     * Extracts unique variables from prompt text.
     *
     * @param promptText prompt text
     * @return ordered list of unique variable names
     */
    public static List<String> extractVariables(
            String promptText) {

        if (promptText == null
                || promptText.isBlank()) {

            return List.of();
        }

        Matcher matcher =
                VARIABLE_PATTERN.matcher(
                        promptText
                );

        Set<String> variables =
                new LinkedHashSet<>();

        while (matcher.find()) {

            String variable =
                    matcher.group(1).trim();

            if (!variable.isBlank()) {
                variables.add(variable);
            }
        }

        return new ArrayList<>(variables);
    }
}