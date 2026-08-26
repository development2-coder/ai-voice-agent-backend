package com.infinitio.aivoiceplatform.callsession.converter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infinitio.aivoiceplatform.callsession.dto.CallConversationMessageDto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts call conversation history between Java objects
 * and JSON database representation.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@Converter
public class CallConversationHistoryConverter
        implements AttributeConverter<
        List<CallConversationMessageDto>,
        String> {

    private final ObjectMapper objectMapper;

    /**
     * Creates conversation history converter.
     */
    public CallConversationHistoryConverter() {

        this.objectMapper =
                new ObjectMapper();

        this.objectMapper.registerModule(
                new JavaTimeModule()
        );

        this.objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    /**
     * Converts conversation history to JSON.
     *
     * @param conversationHistory conversation history
     * @return JSON representation
     */
    @Override
    public String convertToDatabaseColumn(
            List<CallConversationMessageDto> conversationHistory) {

        if (conversationHistory == null) {
            return null;
        }

        try {

            return objectMapper.writeValueAsString(
                    conversationHistory
            );

        } catch (JsonProcessingException exception) {

            log.error(
                    "Failed to serialize call conversation history.",
                    exception
            );

            throw new IllegalArgumentException(
                    "Failed to serialize call conversation history.",
                    exception
            );
        }
    }

    /**
     * Converts JSON to conversation history.
     *
     * @param databaseValue JSON representation
     * @return conversation history
     */
    @Override
    public List<CallConversationMessageDto>
    convertToEntityAttribute(
            String databaseValue) {

        if (databaseValue == null
                || databaseValue.isBlank()) {

            return List.of();
        }

        try {

            return objectMapper.readValue(
                    databaseValue,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    CallConversationMessageDto.class
                            )
            );

        } catch (JsonProcessingException exception) {

            log.error(
                    "Failed to deserialize call conversation history.",
                    exception
            );

            throw new IllegalArgumentException(
                    "Failed to deserialize call conversation history.",
                    exception
            );
        }
    }
}