package com.infinitio.aivoiceplatform.flow.service.impl.node;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowContextService;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.service.TtsRuntimeService;
import com.infinitio.aivoiceplatform.tts.streaming.TtsAudioStreamListener;
import com.infinitio.aivoiceplatform.tts.streaming.TtsAudioStreamRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Flow node handler for Text-to-Speech processing.
 *
 * <p>
 * The handler delegates speech synthesis to the existing
 * {@link TtsRuntimeService}. Provider-specific implementation
 * remains outside the Flow module.
 * </p>
 *
 * <p>
 * The TTS configuration is defined by the client while building
 * the Flow. Runtime values such as language, speaker, pace and
 * sample rate are therefore read from the node configuration
 * or the existing Flow context.
 * </p>
 *
 * <p>
 * The node supports different text sources so that it can be
 * used in both user-first and AI-first client-defined flows.
 * </p>
 *
 * <p>
 * Text resolution order:
 * </p>
 *
 * <ol>
 *     <li>Text explicitly configured on the TTS node</li>
 *     <li>ttsText from Flow context</li>
 *     <li>llmResponse from Flow context</li>
 *     <li>response from Flow context</li>
 * </ol>
 *
 * <p>
 * Supported node configuration values:
 * </p>
 *
 * <pre>
 * text
 * language
 * speaker
 * pace
 * speechSampleRate
 * finalResponse
 * </pre>
 *
 * <p>
 * Generated response values are stored in Flow context:
 * </p>
 *
 * <pre>
 * ttsResponse
 * lastTtsResponse
 * ttsAudioBase64
 * ttsAudioUrl
 * ttsAudioFileName
 * ttsAudioContentType
 * language
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

    /**
     * Call identifier context key.
     */
    private static final String CALL_ID =
            "callId";

    /**
     * Language configuration/context key.
     */
    private static final String LANGUAGE =
            "language";

    /**
     * Explicit TTS text configuration key.
     */
    private static final String TEXT =
            "text";

    /**
     * LLM response context key.
     */
    private static final String LLM_RESPONSE =
            "llmResponse";

    /**
     * Generic response context key.
     */
    private static final String RESPONSE =
            "response";

    /**
     * Runtime TTS text context key.
     */
    private static final String TTS_TEXT =
            "ttsText";

    /**
     * TTS speaker configuration key.
     */
    private static final String SPEAKER =
            "speaker";

    /**
     * TTS pace configuration key.
     */
    private static final String PACE =
            "pace";

    /**
     * TTS speech sample-rate configuration key.
     */
    private static final String SPEECH_SAMPLE_RATE =
            "speechSampleRate";

    /**
     * Final-response configuration key.
     */
    private static final String FINAL_RESPONSE =
            "finalResponse";

    /**
     * TTS response context key.
     */
    private static final String TTS_RESPONSE =
            "ttsResponse";

    /**
     * Last TTS response context key.
     */
    private static final String LAST_TTS_RESPONSE =
            "lastTtsResponse";

    /**
     * TTS audio Base64 context key.
     */
    private static final String TTS_AUDIO_BASE64 =
            "ttsAudioBase64";

    /**
     * TTS audio URL context key.
     */
    private static final String TTS_AUDIO_URL =
            "ttsAudioUrl";

    /**
     * TTS audio file-name context key.
     */
    private static final String TTS_AUDIO_FILE_NAME =
            "ttsAudioFileName";

    /**
     * TTS audio content-type context key.
     */
    private static final String TTS_AUDIO_CONTENT_TYPE =
            "ttsAudioContentType";

    /**
     * Flow node action.
     */
    private static final String ACTION =
            "TTS";

    /**
     * JSON role used for configuration parsing.
     */
    private static final String CONFIGURATION =
            "configuration";

    /**
     * JSON mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Flow context service used for expression resolution.
     */
    private final FlowContextService flowContextService;

    /**
     * Existing provider-independent TTS runtime service.
     */
    private final TtsRuntimeService ttsRuntimeService;

    /**
     * Registry containing active TTS audio stream listeners.
     */
    private final TtsAudioStreamRegistry ttsAudioStreamRegistry;

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
     * <p>
     * The node does not assume that an LLM node must execute before
     * it. It can synthesize explicitly configured text, runtime
     * TTS text, an LLM response, or another Flow response.
     * </p>
     *
     * <p>
     * When the current call has an active TTS stream listener,
     * generated audio is forwarded incrementally to the listener.
     * Otherwise the existing synchronous TTS execution path is used.
     * </p>
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

        Map<String, Object> configuration =
                readConfiguration(
                        node.getConfiguration()
                );

        String callId =
                getRequiredContextString(
                        context,
                        CALL_ID
                );

        String language =
                resolveLanguage(
                        configuration,
                        context
                );

        String text =
                resolveText(
                        configuration,
                        context
                );

        String speaker =
                resolveSpeaker(
                        configuration,
                        context
                );

        Double pace =
                resolveDouble(
                        configuration,
                        PACE
                );

        Integer speechSampleRate =
                resolveInteger(
                        configuration,
                        SPEECH_SAMPLE_RATE
                );

        boolean finalResponse =
                getBooleanConfigurationValue(
                        configuration,
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
                        "pace={}, speechSampleRate={}, " +
                        "finalResponse={}, textLength={}",
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

        /*
         * Check whether the current live call has a registered
         * TTS audio listener.
         *
         * If present, audio is streamed directly to the listener
         * while the provider is producing it.
         */
        TtsAudioStreamListener streamListener =
                ttsAudioStreamRegistry.getListener(
                        callId
                );

        TtsSynthesisResponse response;

        if (streamListener != null) {

            log.info(
                    "Starting streaming TTS execution for Flow node. " +
                            "executionPublicId={}, nodeKey={}, callId={}",
                    execution.getPublicId(),
                    node.getNodeKey(),
                    callId
            );

            /*
             * A new TTS response starts with a clean interruption
             * state. This is important after a previous caller
             * barge-in.
             */
            ttsAudioStreamRegistry.resetInterruption(
                    callId
            );

            /*
             * Protect the active stream from sending audio after
             * a caller has interrupted the TTS response.
             */
            TtsAudioStreamListener guardedListener =
                    (audioBytes, contentType) -> {

                        if (ttsAudioStreamRegistry
                                .isInterrupted(callId)) {

                            log.debug(
                                    "Ignoring interrupted TTS audio chunk. " +
                                            "callId={}, chunkSizeBytes={}",
                                    callId,
                                    audioBytes != null
                                            ? audioBytes.length
                                            : 0
                            );

                            return;
                        }

                        if (audioBytes == null
                                || audioBytes.length == 0) {

                            log.debug(
                                    "Ignoring empty TTS audio chunk. callId={}",
                                    callId
                            );

                            return;
                        }

                        streamListener.onAudioChunk(
                                audioBytes,
                                contentType
                        );
                    };

            response =
                    ttsRuntimeService.synthesizeStreaming(
                            request,
                            guardedListener
                    );

        } else {

            log.debug(
                    "No active TTS stream listener found. " +
                            "Using synchronous TTS execution. callId={}",
                    callId
            );

            response =
                    ttsRuntimeService.synthesize(
                            request
                    );
        }

        validateResponse(
                response
        );

        storeResponse(
                context,
                response
        );

        log.info(
                "TTS Flow node completed. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "provider={}, model={}, speaker={}, " +
                        "contentType={}, latencyMs={}, " +
                        "audioUrlPresent={}, audioBase64Present={}, streaming={}",
                execution.getPublicId(),
                node.getNodeKey(),
                response.getProvider(),
                response.getModel(),
                response.getSpeaker(),
                response.getContentType(),
                response.getLatencyMs(),
                response.getAudioUrl() != null,
                response.getAudioBase64() != null,
                streamListener != null
        );

        /*
         * outputText is intentionally not populated with the audio
         * URL. The audio data is already available in Flow context
         * and is consumed by ConversationResponseService.
         *
         * Keeping outputText null prevents an audio URL from being
         * interpreted as text that should be spoken by the
         * ConversationResponseService.
         */
        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .action(
                        ACTION
                )
                .outputText(
                        null
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
    // LANGUAGE
    // =========================================================

    /**
     * Resolves the TTS language.
     *
     * <p>
     * The node configuration takes precedence. If it is not
     * configured, the language already established in the Flow
     * context is used.
     * </p>
     *
     * @param configuration TTS node configuration
     * @param context Flow context
     * @return language or null
     */
    private String resolveLanguage(
            Map<String, Object> configuration,
            Map<String, Object> context) {

        String configuredLanguage =
                getConfigurationString(
                        configuration,
                        LANGUAGE
                );

        if (configuredLanguage != null) {

            return flowContextService.replaceVariables(
                    configuredLanguage,
                    context
            );
        }

        return getOptionalContextString(
                context,
                LANGUAGE
        );
    }

    // =========================================================
    // TEXT
    // =========================================================

    /**
     * Resolves the text that should be synthesized.
     *
     * <p>
     * The client can explicitly configure text on the node.
     * If no text is configured, runtime values are checked.
     * </p>
     *
     * @param configuration TTS node configuration
     * @param context Flow context
     * @return text to synthesize
     */
    private String resolveText(
            Map<String, Object> configuration,
            Map<String, Object> context) {

        String configuredText =
                getConfigurationString(
                        configuration,
                        TEXT
                );

        if (configuredText != null) {

            String resolvedText =
                    flowContextService.replaceVariables(
                            configuredText,
                            context
                    );

            if (resolvedText != null
                    && !resolvedText.isBlank()) {

                return resolvedText.trim();
            }
        }

        String ttsText =
                getOptionalContextString(
                        context,
                        TTS_TEXT
                );

        if (ttsText != null) {
            return ttsText;
        }

        String llmResponse =
                getOptionalContextString(
                        context,
                        LLM_RESPONSE
                );

        if (llmResponse != null) {
            return llmResponse;
        }

        String response =
                getOptionalContextString(
                        context,
                        RESPONSE
                );

        if (response != null) {
            return response;
        }

        log.warn(
                "No text available for TTS Flow node. " +
                        "executionContextKeys={}",
                context.keySet()
        );

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    // =========================================================
    // SPEAKER
    // =========================================================

    /**
     * Resolves the configured TTS speaker.
     *
     * @param configuration TTS node configuration
     * @param context Flow context
     * @return speaker or null
     */
    private String resolveSpeaker(
            Map<String, Object> configuration,
            Map<String, Object> context) {

        String configuredSpeaker =
                getConfigurationString(
                        configuration,
                        SPEAKER
                );

        if (configuredSpeaker != null) {

            return flowContextService.replaceVariables(
                    configuredSpeaker,
                    context
            );
        }

        return getOptionalContextString(
                context,
                SPEAKER
        );
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    /**
     * Stores the TTS response in Flow context.
     *
     * @param context Flow context
     * @param response TTS response
     */
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
         * Preserve the effective language returned by the TTS
         * runtime for downstream nodes.
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
    // CONFIGURATION
    // =========================================================

    /**
     * Reads the TTS node JSON configuration.
     *
     * @param configuration JSON configuration
     * @return configuration map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfiguration(
            String configuration) {

        if (configuration == null
                || configuration.isBlank()) {

            return new HashMap<>();
        }

        try {

            return objectMapper.readValue(
                    configuration,
                    Map.class
            );

        } catch (Exception exception) {

            log.error(
                    "Unable to parse TTS node configuration.",
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Gets a configuration string.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @return value or null
     */
    private String getConfigurationString(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(
                        key
                );

        if (value == null) {
            return null;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        return result.isBlank()
                ? null
                : result;
    }

    /**
     * Resolves a Double configuration value.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @return Double value or null
     */
    private Double resolveDouble(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(
                        key
                );

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {

            return number.doubleValue();
        }

        String stringValue =
                String.valueOf(
                        value
                ).trim();

        if (stringValue.isBlank()) {
            return null;
        }

        try {

            return Double.parseDouble(
                    stringValue
            );

        } catch (NumberFormatException exception) {

            log.warn(
                    "Invalid TTS numeric configuration. " +
                            "key={}, value={}",
                    key,
                    value
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Resolves an Integer configuration value.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @return Integer value or null
     */
    private Integer resolveInteger(
            Map<String, Object> configuration,
            String key) {

        Object value =
                configuration.get(
                        key
                );

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {

            return number.intValue();
        }

        String stringValue =
                String.valueOf(
                        value
                ).trim();

        if (stringValue.isBlank()) {
            return null;
        }

        try {

            return Integer.parseInt(
                    stringValue
            );

        } catch (NumberFormatException exception) {

            log.warn(
                    "Invalid TTS integer configuration. " +
                            "key={}, value={}",
                    key,
                    value
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Reads a Boolean configuration value.
     *
     * @param configuration node configuration
     * @param key configuration key
     * @param defaultValue default value
     * @return Boolean value
     */
    private boolean getBooleanConfigurationValue(
            Map<String, Object> configuration,
            String key,
            boolean defaultValue) {

        Object value =
                configuration.get(
                        key
                );

        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean booleanValue) {

            return booleanValue;
        }

        String stringValue =
                String.valueOf(
                        value
                ).trim();

        if (stringValue.isBlank()) {

            return defaultValue;
        }

        return Boolean.parseBoolean(
                stringValue
        );
    }

    // =========================================================
    // CONTEXT
    // =========================================================

    /**
     * Gets a required context string.
     *
     * @param context Flow context
     * @param key context key
     * @return context value
     */
    private String getRequiredContextString(
            Map<String, Object> context,
            String key) {

        String value =
                getOptionalContextString(
                        context,
                        key
                );

        if (value == null) {

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        return value;
    }

    /**
     * Gets an optional context string.
     *
     * @param context Flow context
     * @param key context key
     * @return context value or null
     */
    private String getOptionalContextString(
            Map<String, Object> context,
            String key) {

        Object value =
                context.get(
                        key
                );

        if (value == null) {
            return null;
        }

        String result =
                String.valueOf(
                        value
                ).trim();

        return result.isBlank()
                ? null
                : result;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    /**
     * Validates Flow execution.
     *
     * @param execution Flow execution
     */
    private void validateExecution(
            FlowExecution execution) {

        if (execution != null) {
            return;
        }

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    /**
     * Validates Flow node.
     *
     * @param node Flow node
     */
    private void validateNode(
            FlowNode node) {

        if (node != null) {
            return;
        }

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    /**
     * Validates Flow context.
     *
     * @param context Flow context
     */
    private void validateContext(
            Map<String, Object> context) {

        if (context != null) {
            return;
        }

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    /**
     * Validates TTS runtime response.
     *
     * @param response TTS response
     */
    private void validateResponse(
            TtsSynthesisResponse response) {

        if (response != null) {
            return;
        }

        log.error(
                "TTS runtime returned null response."
        );

        throw new IllegalStateException(
                FlowMessages.EXECUTION_FAILED
        );
    }
}