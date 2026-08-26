package com.infinitio.aivoiceplatform.flow.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowContextServiceImpl
        implements FlowContextService {

    private final ObjectMapper objectMapper;

    @Override
    public String writeContext(
            Map<String, Object> context) {

        try {

            if (context == null) {
                context = new HashMap<>();
            }

            return objectMapper.writeValueAsString(
                    context
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to serialize flow context.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to serialize flow context.",
                    exception
            );
        }
    }

    @Override
    public Map<String, Object> readContext(
            String contextData) {

        try {

            if (contextData == null
                    || contextData.isBlank()) {

                return new HashMap<>();
            }

            return objectMapper.readValue(
                    contextData,
                    new TypeReference<
                            Map<String, Object>>() {
                    }
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to deserialize flow context.",
                    exception
            );

            throw new IllegalStateException(
                    "Unable to read flow context.",
                    exception
            );
        }
    }

    @Override
    public void setVariable(
            Map<String, Object> context,
            String key,
            Object value) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "Flow context cannot be null."
            );
        }

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Flow context variable key cannot be blank."
            );
        }

        context.put(
                key,
                value
        );
    }

    @Override
    public Object getVariable(
            Map<String, Object> context,
            String key) {

        if (context == null
                || key == null
                || key.isBlank()) {

            return null;
        }

        return context.get(key);
    }

    @Override
    public String getStringVariable(
            Map<String, Object> context,
            String key) {

        Object value =
                getVariable(
                        context,
                        key
                );

        return value == null
                ? null
                : String.valueOf(value);
    }

    @Override
    public String replaceVariables(
            String text,
            Map<String, Object> context) {

        if (text == null
                || text.isBlank()
                || context == null
                || context.isEmpty()) {

            return text;
        }

        String result = text;

        for (Map.Entry<String, Object> entry :
                context.entrySet()) {

            String placeholder =
                    "{{" + entry.getKey() + "}}";

            String value =
                    entry.getValue() == null
                            ? ""
                            : String.valueOf(
                            entry.getValue()
                    );

            result = result.replace(
                    placeholder,
                    value
            );
        }

        return result;
    }
}