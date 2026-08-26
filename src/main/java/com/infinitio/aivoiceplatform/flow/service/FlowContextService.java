package com.infinitio.aivoiceplatform.flow.service;

import java.util.Map;

public interface FlowContextService {

    String writeContext(
            Map<String, Object> context
    );

    Map<String, Object> readContext(
            String contextData
    );

    void setVariable(
            Map<String, Object> context,
            String key,
            Object value
    );

    Object getVariable(
            Map<String, Object> context,
            String key
    );

    String getStringVariable(
            Map<String, Object> context,
            String key
    );

    String replaceVariables(
            String text,
            Map<String, Object> context
    );
}