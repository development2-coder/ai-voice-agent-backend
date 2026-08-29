package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.service.TtsRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Flow node handler for Text-to-Speech processing.
 *
 * <p>
 * The handler delegates speech synthesis to the existing
 * TtsRuntimeService. Provider-specific implementation remains
 * outside the Flow module.
 * </p>
 *
 * <p>
 * Expected Flow context:
 * </p>
 *
 * <pre>
 * {
 *     "callId": "...",
 *     "language": "en-IN",
 *     "llmResponse": "Hello, how can I help you?",
 *     "ttsSpeaker": "shubh",
 *     "ttsPace": 1.0,
 *     "ttsSpeechSampleRate": 22050,
 *     "finalResponse": true
 * }
 * </pre>
 *
 * <p>
 * The generated response is stored in:
 * </p>
 *
 * <pre>
 * ttsResponse
 * ttsAudioBase64
 * ttsAudioUrl
 * ttsAudioFileName
 * ttsAudioContentType
 * lastTtsResponse
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TtsNodeHandler
        implements FlowNodeHandler {

    private static final String CALL_ID =
            "callId";

    private static final String LANGUAGE =
            "language";

    private static final String LLM_RESPONSE =
            "llmResponse";

    private static final String RESPONSE =
            "response";

    private static final String TTS_TEXT =
            "ttsText";

    private static final String TTS_SPEAKER =
            "ttsSpeaker";

    private static final String SPEAKER =
            "speaker";

    private static final String TTS_PACE =
            "ttsPace";

    private static final String TTS_SAMPLE_RATE =
            "ttsSpeechSampleRate";

    private static final String FINAL_RESPONSE =
            "finalResponse";

    private static final String TTS_RESPONSE =
            "ttsResponse";

    private static final String LAST_TTS_RESPONSE =
            "lastTtsResponse";

    private static final String TTS_AUDIO_BASE64 =
            "ttsAudioBase64";

    private static final String TTS_AUDIO_URL =
            "ttsAudioUrl";

    private static final String TTS_AUDIO_FILE_NAME =
            "ttsAudioFileName";

    private static final String TTS_AUDIO_CONTENT_TYPE =
            "ttsAudioContentType";

    private static final String ACTION =
            "TTS";

    private static final String DEFAULT_LANGUAGE =
            "en-IN";

    private static final String DEFAULT_SPEAKER =
            "shubh";

    /**
     * Existing provider-independent TTS runtime service.
     */
    private final TtsRuntimeService ttsRuntimeService;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.TTS;
    }

    /**
     * Executes the TTS Flow node.
     *
     * @param execution current Flow execution
     * @param node current Flow node
     * @param context current Flow context
     * @return Flow node execution result
     */
    @Override
    public FlowNodeExecutionResult handle(
            FlowExecution execution,
            FlowNode node,
            Map<String, Object> context) {

        validateExecution(
                execution
        );

        validateNode(
                node
        );

        validateContext(
                context
        );

        log.info(
                "Executing TTS Flow node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        String callId =
                getRequiredString(
                        context,
                        CALL_ID
                );

        String language =
                getStringOrDefault(
                        context,
                        LANGUAGE,
                        null,
                        DEFAULT_LANGUAGE
                );

        String text =
                resolveText(
                        context
                );

        String speaker =
                getStringOrDefault(
                        context,
                        TTS_SPEAKER,
                        SPEAKER,
                        DEFAULT_SPEAKER
                );

        Double pace =
                getDoubleValue(
                        context,
                        TTS_PACE
                );

        Integer speechSampleRate =
                getIntegerValue(
                        context,
                        TTS_SAMPLE_RATE
                );

        boolean finalResponse =
                getBooleanValue(
                        context,
                        FINAL_RESPONSE,
                        true
                );

        TtsSynthesisRequest request =
                TtsSynthesisRequest.builder()
                        .callId(
                                callId
                        )
                        .language(
                                language
                        )
                        .speaker(
                                speaker
                        )
                        .text(
                                text
                        )
                        .pace(
                                pace
                        )
                        .speechSampleRate(
                                speechSampleRate
                        )
                        .finalResponse(
                                finalResponse
                        )
                        .build();

        log.debug(
                "Calling TTS runtime service. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "callId={}, language={}, speaker={}, " +
                        "pace={}, speechSampleRate={}, finalResponse={}, " +
                        "textLength={}",
                execution.getPublicId(),
                node.getNodeKey(),
                callId,
                language,
                speaker,
                pace,
                speechSampleRate,
                finalResponse,
                text.length()
        );

        TtsSynthesisResponse response =
                ttsRuntimeService.synthesize(
                        request
                );

        if (response == null) {

            log.error(
                    "TTS runtime returned null response. " +
                            "executionPublicId={}, nodeKey={}",
                    execution.getPublicId(),
                    node.getNodeKey()
            );

            throw new IllegalStateException(
                    FlowMessages.EXECUTION_FAILED
            );
        }

        storeResponse(
                context,
                response
        );

        log.info(
                "TTS Flow node completed. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "provider={}, model={}, speaker={}, " +
                        "contentType={}, latencyMs={}, " +
                        "audioUrlPresent={}, audioBase64Present={}",
                execution.getPublicId(),
                node.getNodeKey(),
                response.getProvider(),
                response.getModel(),
                response.getSpeaker(),
                response.getContentType(),
                response.getLatencyMs(),
                response.getAudioUrl() != null,
                response.getAudioBase64() != null
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .action(
                        ACTION
                )
                .outputText(
                        response.getAudioUrl()
                )
                .waiting(false)
                .completed(false)
                .transferred(false)
                .context(
                        context
                )
                .build();
    }

    // =========================================================
    // TEXT
    // =========================================================

    /**
     * Resolves the text that should be synthesized.
     *
     * <p>
     * llmResponse is the primary source because the normal
     * voice-agent flow is STT -> LLM -> TTS.
     * </p>
     */
    private String resolveText(
            Map<String, Object> context) {

        Object ttsText =
                context.get(
                        TTS_TEXT
                );

        if (ttsText != null) {

            String value =
                    String.valueOf(
                            ttsText
                    ).trim();

            if (!value.isBlank()) {
                return value;
            }
        }

        Object llmResponse =
                context.get(
                        LLM_RESPONSE
                );

        if (llmResponse != null) {

            String value =
                    String.valueOf(
                            llmResponse
                    ).trim();

            if (!value.isBlank()) {
                return value;
            }
        }

        Object response =
                context.get(
                        RESPONSE
                );

        if (response != null) {

            String value =
                    String.valueOf(
                            response
                    ).trim();

            if (!value.isBlank()) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private void storeResponse(
            Map<String, Object> context,
            TtsSynthesisResponse response) {

        context.put(
                TTS_RESPONSE,
                response
        );

        context.put(
                LAST_TTS_RESPONSE,
                response
        );

        context.put(
                TTS_AUDIO_BASE64,
                response.getAudioBase64()
        );

        context.put(
                TTS_AUDIO_URL,
                response.getAudioUrl()
        );

        context.put(
                TTS_AUDIO_FILE_NAME,
                response.getFileName()
        );

        context.put(
                TTS_AUDIO_CONTENT_TYPE,
                response.getContentType()
        );

        /*
         * Preserve the language returned by the TTS runtime.
         */
        if (response.getLanguage() != null
                && !response.getLanguage().isBlank()) {

            context.put(
                    LANGUAGE,
                    response.getLanguage()
            );
        }
    }

    // =========================================================
    // CONTEXT HELPERS
    // =========================================================

    private String getRequiredString(
            Map<String, Object> context,
            String key) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        if (result.isBlank()) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return result;
    }

    private String getStringOrDefault(
            Map<String, Object> context,
            String primaryKey,
            String secondaryKey,
            String defaultValue) {

        Object primary =
                context.get(
                        primaryKey
                );

        if (primary != null) {

            String result =
                    String.valueOf(
                            primary
                    ).trim();

            if (!result.isBlank()) {
                return result;
            }
        }

        if (secondaryKey != null) {

            Object secondary =
                    context.get(
                            secondaryKey
                    );

            if (secondary != null) {

                String result =
                        String.valueOf(
                                secondary
                        ).trim();

                if (!result.isBlank()) {
                    return result;
                }
            }
        }

        return defaultValue;
    }

    private Double getDoubleValue(
            Map<String, Object> context,
            String key) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {

            return Double.parseDouble(
                    String.valueOf(
                            value
                    )
            );

        } catch (NumberFormatException exception) {

            log.warn(
                    "Invalid TTS numeric value. key={}, value={}",
                    key,
                    value
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    private Integer getIntegerValue(
            Map<String, Object> context,
            String key) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {

            return Integer.parseInt(
                    String.valueOf(
                            value
                    )
            );

        } catch (NumberFormatException exception) {

            log.warn(
                    "Invalid TTS sample rate. key={}, value={}",
                    key,
                    value
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    private boolean getBooleanValue(
            Map<String, Object> context,
            String key,
            boolean defaultValue) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.parseBoolean(
                String.valueOf(
                        value
                )
        );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateExecution(
            FlowExecution execution) {

        if (execution != null) {
            return;
        }

        throw new IllegalArgumentException(
                "Flow execution cannot be null."
        );
    }

    private void validateNode(
            FlowNode node) {

        if (node != null) {
            return;
        }

        throw new IllegalArgumentException(
                "Flow node cannot be null."
        );
    }

    private void validateContext(
            Map<String, Object> context) {

        if (context != null) {
            return;
        }

        throw new IllegalArgumentException(
                "Flow execution context cannot be null."
        );
    }
}