package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.flow.service.FlowRuntimeConfigurationResolver;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

/**
 * Flow node handler for Speech-to-Text processing.
 *
 * <p>
 * The handler delegates transcription to the existing STT runtime
 * abstraction. It does not directly communicate with Sarvam or
 * another provider.
 * </p>
 *
 * <p>
 * Configuration priority:
 * </p>
 *
 * <pre>
 * Flow Node Configuration
 *          ↓
 * Agent Runtime Configuration
 *          ↓
 * Flow Context
 * </pre>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SttNodeHandler
        implements FlowNodeHandler {

    private static final String CALL_ID =
            "callId";

    private static final String AUDIO =
            "audio";

    private static final String AUDIO_CONTENT_TYPE =
            "audioContentType";

    private static final String CONTENT_TYPE =
            "contentType";

    private static final String AUDIO_FILE_NAME =
            "audioFileName";

    private static final String FILE_NAME =
            "fileName";

    private static final String LANGUAGE =
            "language";

    private static final String FINAL_TRANSCRIPT =
            "finalTranscript";

    private static final String TRANSCRIPT =
            "transcript";

    private static final String LAST_TRANSCRIPT =
            "lastTranscript";

    private static final String STT_RESPONSE =
            "sttResponse";

    private static final String CONFIGURATION =
            "configuration";

    private static final String DEFAULT_CONTENT_TYPE =
            "audio/wav";

    private static final String DEFAULT_FILE_NAME =
            "flow-audio.wav";

    private final SttRuntimeService sttRuntimeService;

    private final FlowRuntimeConfigurationResolver
            runtimeConfigurationResolver;

    private final ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public FlowNodeType getNodeType() {

        return FlowNodeType.STT;
    }

    /**
     * Executes the STT node.
     *
     * @param execution current Flow execution
     * @param node current Flow node
     * @param context current Flow context
     * @return STT execution result
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
                "Executing STT Flow node. " +
                        "executionPublicId={}, nodeKey={}",
                execution.getPublicId(),
                node.getNodeKey()
        );

        Map<String, Object> nodeConfiguration =
                resolveNodeConfiguration(
                        node
                );

        String callId =
                getRequiredString(
                        context,
                        CALL_ID
                );

        byte[] audio =
                resolveAudio(
                        context
                );

        String contentType =
                getStringOrDefault(
                        context,
                        AUDIO_CONTENT_TYPE,
                        CONTENT_TYPE,
                        DEFAULT_CONTENT_TYPE
                );

        String fileName =
                getStringOrDefault(
                        context,
                        AUDIO_FILE_NAME,
                        FILE_NAME,
                        DEFAULT_FILE_NAME
                );

        String language =
                resolveLanguage(
                        nodeConfiguration,
                        context
                );

        boolean finalTranscript =
                getBooleanValue(
                        context,
                        FINAL_TRANSCRIPT,
                        true
                );

        SttTranscriptionRequest request =
                SttTranscriptionRequest.builder()
                        .callId(
                                callId
                        )
                        .audio(
                                audio
                        )
                        .contentType(
                                contentType
                        )
                        .fileName(
                                fileName
                        )
                        .language(
                                language
                        )
                        .finalTranscript(
                                finalTranscript
                        )
                        .build();

        log.debug(
                "Calling STT runtime service. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "callId={}, language={}, " +
                        "finalTranscript={}, audioBytes={}",
                execution.getPublicId(),
                node.getNodeKey(),
                callId,
                language,
                finalTranscript,
                audio.length
        );

        SttTranscriptionResponse response;

        try {

            response =
                    sttRuntimeService.transcribe(
                            request
                    );

        } catch (Exception exception) {

            log.error(
                    "STT runtime execution failed. " +
                            "executionPublicId={}, nodeKey={}, " +
                            "callId={}, language={}",
                    execution.getPublicId(),
                    node.getNodeKey(),
                    callId,
                    language,
                    exception
            );

            throw new IllegalStateException(
                    FlowMessages.EXECUTION_FAILED,
                    exception
            );
        }

        if (response == null) {

            log.error(
                    "STT runtime returned null response. " +
                            "executionPublicId={}, nodeKey={}, " +
                            "callId={}",
                    execution.getPublicId(),
                    node.getNodeKey(),
                    callId
            );

            throw new IllegalStateException(
                    FlowMessages.EXECUTION_FAILED
            );
        }

        String transcript =
                response.getTranscript();

        if (transcript == null) {

            transcript = "";
        }

        context.put(
                TRANSCRIPT,
                transcript
        );

        context.put(
                LAST_TRANSCRIPT,
                transcript
        );

        context.put(
                STT_RESPONSE,
                response
        );

        /*
         * Preserve the provider returned language when available.
         */
        if (response.getLanguage() != null
                && !response.getLanguage().isBlank()) {

            context.put(
                    LANGUAGE,
                    response.getLanguage()
            );
        }

        log.info(
                "STT Flow node completed. " +
                        "executionPublicId={}, nodeKey={}, " +
                        "provider={}, finalTranscript={}, " +
                        "latencyMs={}, transcriptLength={}",
                execution.getPublicId(),
                node.getNodeKey(),
                response.getProvider(),
                response.isFinalTranscript(),
                response.getLatencyMs(),
                transcript.length()
        );

        return FlowNodeExecutionResult.builder()
                .status(
                        FlowExecutionStatus.RUNNING
                )
                .action(
                        "STT"
                )
                .outputText(
                        transcript
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
    // NODE CONFIGURATION
    // =========================================================

    /**
     * Resolves the JSON configuration stored on the Flow node.
     *
     * @param node Flow node
     * @return node configuration
     */
    private Map<String, Object> resolveNodeConfiguration(
            FlowNode node) {

        /*
         * IMPORTANT:
         *
         * Use the getter that actually exists on your FlowNode
         * entity. The current uploaded handler did not extract
         * node configuration before using nodeConfiguration.
         *
         * If your entity stores JSON under getConfiguration(),
         * this block works directly.
         */
        String configuration =
                node.getConfiguration();

        if (configuration == null
                || configuration.isBlank()) {

            return Collections.emptyMap();
        }

        try {

            Map<String, Object> parsedConfiguration =
                    objectMapper.readValue(
                            configuration,
                            new TypeReference<Map<String, Object>>() {
                            }
                    );

            if (parsedConfiguration == null) {

                return Collections.emptyMap();
            }

            /*
             * Some Flow definitions may wrap node values inside
             * a "configuration" object.
             */
            Object nestedConfiguration =
                    parsedConfiguration.get(
                            CONFIGURATION
                    );

            if (nestedConfiguration instanceof Map<?, ?> nestedMap) {

                Map<String, Object> result =
                        new java.util.HashMap<>();

                nestedMap.forEach(
                        (key, value) ->
                                result.put(
                                        String.valueOf(key),
                                        value
                                )
                );

                return result;
            }

            return parsedConfiguration;

        } catch (Exception exception) {

            log.error(
                    "Unable to parse STT node configuration. " +
                            "nodeKey={}",
                    node.getNodeKey(),
                    exception
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION,
                    exception
            );
        }
    }

    /**
     * Resolves the language used by STT.
     *
     * <p>
     * Priority:
     * </p>
     *
     * <pre>
     * Node language
     *      ↓
     * Agent runtime language
     *      ↓
     * Existing Flow context language
     * </pre>
     *
     * @param nodeConfiguration node configuration
     * @param context Flow context
     * @return resolved language
     */
    private String resolveLanguage(
            Map<String, Object> nodeConfiguration,
            Map<String, Object> context) {

        Object nodeLanguage =
                nodeConfiguration.get(
                        LANGUAGE
                );

        if (hasValue(
                nodeLanguage
        )) {

            String language =
                    String.valueOf(
                            nodeLanguage
                    ).trim();

            log.debug(
                    "Using STT node language override. language={}",
                    language
            );

            return language;
        }

        Object agentLanguage =
                runtimeConfigurationResolver.resolve(
                        nodeConfiguration,
                        context,
                        LANGUAGE,
                        null
                );

        if (hasValue(
                agentLanguage
        )) {

            String language =
                    String.valueOf(
                            agentLanguage
                    ).trim();

            log.debug(
                    "Using Agent Configuration STT language. " +
                            "language={}",
                    language
            );

            return language;
        }

        Object contextLanguage =
                context.get(
                        LANGUAGE
                );

        if (hasValue(
                contextLanguage
        )) {

            String language =
                    String.valueOf(
                            contextLanguage
                    ).trim();

            log.debug(
                    "Using Flow context STT language. language={}",
                    language
            );

            return language;
        }

        log.error(
                "No STT language is configured. nodeKey={}",
                nodeConfiguration
        );

        throw new IllegalArgumentException(
                "STT language is not configured."
        );
    }

    /**
     * Checks whether a configuration value exists.
     *
     * @param value configuration value
     * @return true when value exists
     */
    private boolean hasValue(
            Object value) {

        if (value == null) {

            return false;
        }

        if (value instanceof String stringValue) {

            return !stringValue.isBlank();
        }

        return true;
    }

    // =========================================================
    // AUDIO
    // =========================================================

    /**
     * Resolves audio from Flow context.
     *
     * @param context Flow context
     * @return audio bytes
     */
    private byte[] resolveAudio(
            Map<String, Object> context) {

        Object audio =
                context.get(
                        AUDIO
                );

        if (audio == null) {

            log.error(
                    "STT audio is missing from Flow context."
            );

            throw new IllegalArgumentException(
                    FlowMessages.INVALID_CONFIGURATION
            );
        }

        if (audio instanceof byte[] bytes) {

            if (bytes.length == 0) {

                throw new IllegalArgumentException(
                        FlowMessages.INVALID_CONFIGURATION
                );
            }

            return bytes;
        }

        if (audio instanceof String base64) {

            if (base64.isBlank()) {

                throw new IllegalArgumentException(
                        FlowMessages.INVALID_CONFIGURATION
                );
            }

            try {

                byte[] decoded =
                        Base64.getDecoder()
                                .decode(
                                        base64
                                );

                if (decoded.length == 0) {

                    throw new IllegalArgumentException(
                            FlowMessages.INVALID_CONFIGURATION
                    );
                }

                return decoded;

            } catch (IllegalArgumentException exception) {

                log.error(
                        "Unable to decode Base64 STT audio.",
                        exception
                );

                throw new IllegalArgumentException(
                        FlowMessages.INVALID_CONFIGURATION,
                        exception
                );
            }
        }

        log.error(
                "Unsupported STT audio type. type={}",
                audio.getClass().getName()
        );

        throw new IllegalArgumentException(
                FlowMessages.INVALID_CONFIGURATION
        );
    }

    // =========================================================
    // CONTEXT
    // =========================================================

    /**
     * Gets a required String value.
     *
     * @param context Flow context
     * @param key context key
     * @return required value
     */
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

    /**
     * Gets a String from the context using primary and secondary
     * keys.
     *
     * @param context Flow context
     * @param primaryKey primary key
     * @param secondaryKey secondary key
     * @param defaultValue fallback value
     * @return resolved value
     */
    private String getStringOrDefault(
            Map<String, Object> context,
            String primaryKey,
            String secondaryKey,
            String defaultValue) {

        Object primary =
                context.get(
                        primaryKey
                );

        if (hasValue(
                primary
        )) {

            return String.valueOf(
                    primary
            ).trim();
        }

        if (secondaryKey != null) {

            Object secondary =
                    context.get(
                            secondaryKey
                    );

            if (hasValue(
                    secondary
            )) {

                return String.valueOf(
                        secondary
                ).trim();
            }
        }

        return defaultValue;
    }

    /**
     * Resolves a boolean context value.
     *
     * @param context Flow context
     * @param key context key
     * @param defaultValue fallback value
     * @return boolean value
     */
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

    /**
     * Validates Flow execution.
     *
     * @param execution Flow execution
     */
    private void validateExecution(
            FlowExecution execution) {

        if (execution == null) {

            throw new IllegalArgumentException(
                    "Flow execution cannot be null."
            );
        }
    }

    /**
     * Validates Flow node.
     *
     * @param node Flow node
     */
    private void validateNode(
            FlowNode node) {

        if (node == null) {

            throw new IllegalArgumentException(
                    "Flow node cannot be null."
            );
        }
    }

    /**
     * Validates Flow context.
     *
     * @param context Flow context
     */
    private void validateContext(
            Map<String, Object> context) {

        if (context == null) {

            throw new IllegalArgumentException(
                    "Flow execution context cannot be null."
            );
        }
    }
}