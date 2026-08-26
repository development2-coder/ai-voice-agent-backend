package com.infinitio.aivoiceplatform.callsession.converter;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts collected call-session slots to and from JSON.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Converter
public class CallSessionSlotsConverter
        implements AttributeConverter<Map<String, String>, String> {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    private static final TypeReference<
            Map<String, String>> TYPE_REFERENCE =
            new TypeReference<>() {
            };

    /**
     * Converts collected slots to JSON before storing them
     * in the database.
     *
     * @param attribute collected slots
     * @return JSON representation
     */
    @Override
    public String convertToDatabaseColumn(
            Map<String, String> attribute) {

        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (Exception exception) {

            log.error(
                    "Unable to serialize call session slots.",
                    exception
            );

            throw new IllegalArgumentException(
                    "Unable to serialize call session slots.",
                    exception
            );
        }
    }

    /**
     * Converts database JSON into collected slots.
     *
     * @param dbData JSON representation
     * @return collected slots
     */
    @Override
    public Map<String, String> convertToEntityAttribute(
            String dbData) {

        if (dbData == null || dbData.isBlank()) {
            return new HashMap<>();
        }

        try {
            return OBJECT_MAPPER.readValue(
                    dbData,
                    TYPE_REFERENCE
            );
        } catch (Exception exception) {

            log.error(
                    "Unable to deserialize call session slots.",
                    exception
            );

            return new HashMap<>();
        }
    }
}