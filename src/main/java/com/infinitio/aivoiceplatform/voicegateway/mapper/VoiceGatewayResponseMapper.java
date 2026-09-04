package com.infinitio.aivoiceplatform.voicegateway.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.voicegateway.constant.VoiceGatewayConstants;
import com.infinitio.aivoiceplatform.voicegateway.dto.response.VoiceGatewayResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts normalized Voice Gateway responses into provider-specific
 * WebSocket messages.
 *
 * <p>
 * The Voice Gateway runtime produces provider-neutral
 * {@link VoiceGatewayResponseDto} objects. This mapper converts those
 * responses into the Exotel WebSocket protocol without exposing
 * provider-specific payload structures to the runtime layer.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceGatewayResponseMapper {

    /**
     * JSON object mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Converts a Voice Gateway response into an Exotel WebSocket
     * message.
     *
     * @param response normalized Voice Gateway response
     * @return provider-specific JSON payload
     */
    public String toProviderMessage(
            VoiceGatewayResponseDto response) {

        if (response == null) {

            throw new IllegalArgumentException(
                    "Voice Gateway response is required."
            );
        }

        try {

            if (response.isClearAudio()) {

                return buildClearMessage(
                        response
                );
            }

            if (response.getAudioBase64() != null
                    && !response.getAudioBase64().isBlank()) {

                return buildMediaMessage(
                        response
                );
            }

            if (response.getMarkName() != null
                    && !response.getMarkName().isBlank()) {

                return buildMarkMessage(
                        response
                );
            }

            log.debug(
                    "No provider WebSocket payload generated. " +
                            "callId={}, action={}",
                    response.getCallId(),
                    response.getAction()
            );

            return null;

        } catch (JsonProcessingException exception) {

            log.error(
                    "Unable to build Exotel WebSocket response. " +
                            "callId={}, streamId={}, action={}",
                    response.getCallId(),
                    response.getStreamId(),
                    response.getAction(),
                    exception
            );

            throw new IllegalStateException(
                    "Unable to build Exotel WebSocket response.",
                    exception
            );
        }
    }

    /**
     * Builds an Exotel media message.
     *
     * @param response normalized Voice Gateway response
     * @return Exotel media JSON
     * @throws JsonProcessingException when JSON serialization fails
     */
    private String buildMediaMessage(
            VoiceGatewayResponseDto response)
            throws JsonProcessingException {

        Map<String, Object> media =
                new LinkedHashMap<>();

        media.put(
                VoiceGatewayConstants.FIELD_PAYLOAD,
                response.getAudioBase64()
        );

        Map<String, Object> message =
                new LinkedHashMap<>();

        message.put(
                VoiceGatewayConstants.FIELD_EVENT,
                VoiceGatewayConstants.OUTBOUND_EVENT_MEDIA
        );

        message.put(
                VoiceGatewayConstants.FIELD_STREAM_SID,
                response.getStreamId()
        );

        message.put(
                VoiceGatewayConstants.FIELD_MEDIA,
                media
        );

        return objectMapper.writeValueAsString(
                message
        );
    }

    /**
     * Builds an Exotel clear message.
     *
     * @param response normalized Voice Gateway response
     * @return Exotel clear JSON
     * @throws JsonProcessingException when JSON serialization fails
     */
    private String buildClearMessage(
            VoiceGatewayResponseDto response)
            throws JsonProcessingException {

        Map<String, Object> message =
                new LinkedHashMap<>();

        message.put(
                VoiceGatewayConstants.FIELD_EVENT,
                VoiceGatewayConstants.OUTBOUND_EVENT_CLEAR
        );

        message.put(
                VoiceGatewayConstants.FIELD_STREAM_SID,
                response.getStreamId()
        );

        return objectMapper.writeValueAsString(
                message
        );
    }

    /**
     * Builds an Exotel mark message.
     *
     * @param response normalized Voice Gateway response
     * @return Exotel mark JSON
     * @throws JsonProcessingException when JSON serialization fails
     */
    private String buildMarkMessage(
            VoiceGatewayResponseDto response)
            throws JsonProcessingException {

        Map<String, Object> mark =
                new LinkedHashMap<>();

        mark.put(
                VoiceGatewayConstants.FIELD_MARK_NAME,
                response.getMarkName()
        );

        Map<String, Object> message =
                new LinkedHashMap<>();

        message.put(
                VoiceGatewayConstants.FIELD_EVENT,
                VoiceGatewayConstants.OUTBOUND_EVENT_MARK
        );

        message.put(
                VoiceGatewayConstants.FIELD_STREAM_SID,
                response.getStreamId()
        );

        message.put(
                VoiceGatewayConstants.FIELD_MARK,
                mark
        );

        return objectMapper.writeValueAsString(
                message
        );
    }
}