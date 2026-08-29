package com.infinitio.aivoiceplatform.flow.service.impl.node;

import com.infinitio.aivoiceplatform.flow.constant.FlowExecutionStatus;
import com.infinitio.aivoiceplatform.flow.constant.FlowMessages;
import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeExecutionResult;
import com.infinitio.aivoiceplatform.flow.entity.FlowExecution;
import com.infinitio.aivoiceplatform.flow.entity.FlowNode;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.service.SttRuntimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

/**
 * Flow node handler for Speech-to-Text processing.
 *
 * <p>
 * The handler uses the existing STT runtime service rather than
 * directly communicating with an STT provider. This keeps Flow
 * execution independent from Sarvam or any other STT provider.
 * </p>
 *
 * <p>
 * Expected runtime context:
 * </p>
 *
 * <pre>
 * {
 *     "callId": "...",
 *     "audio": byte[],
 *     "audioContentType": "audio/wav",
 *     "audioFileName": "input.wav",
 *     "language": "en-IN",
 *     "finalTranscript": true
 * }
 * </pre>
 *
 * <p>
 * The resulting transcript is stored in the context using:
 * </p>
 *
 * <pre>
 * transcript
 * lastTranscript
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

    private static final String DEFAULT_CONTENT_TYPE =
            "audio/wav";

    private static final String DEFAULT_FILE_NAME =
            "flow-audio.wav";

    private static final String DEFAULT_LANGUAGE =
            "en-IN";

    /**
     * Existing STT runtime abstraction.
     */
    private final SttRuntimeService sttRuntimeService;

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
     * @return node execution result
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

        String callId =
                getStringValue(
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
                getStringOrDefault(
                        context,
                        LANGUAGE,
                        null,
                        DEFAULT_LANGUAGE
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
                        "callId={}, contentType={}, fileName={}, " +
                        "language={}, finalTranscript={}, audioBytes={}",
                execution.getPublicId(),
                node.getNodeKey(),
                callId,
                contentType,
                fileName,
                language,
                finalTranscript,
                audio.length
        );

        SttTranscriptionResponse response =
                sttRuntimeService.transcribe(
                        request
                );

        if (response == null) {

            log.error(
                    "STT runtime returned null response. " +
                            "executionPublicId={}, nodeKey={}",
                    execution.getPublicId(),
                    node.getNodeKey()
            );

            throw new IllegalStateException(
                    FlowMessages.EXECUTION_FAILED
            );
        }

        String transcript =
                response.getTranscript();

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
         * Preserve returned language because the STT provider may
         * detect or normalize the language.
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
                        "provider={}, finalTranscript={}, latencyMs={}, " +
                        "transcriptLength={}",
                execution.getPublicId(),
                node.getNodeKey(),
                response.getProvider(),
                response.isFinalTranscript(),
                response.getLatencyMs(),
                transcript == null
                        ? 0
                        : transcript.length()
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
    // AUDIO
    // =========================================================

    /**
     * Resolves audio from the runtime Flow context.
     *
     * <p>
     * Supports byte[] directly and Base64 encoded String values.
     * This allows the Flow runtime to work with either binary
     * media supplied by the Call Session or serialized media.
     * </p>
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

    private String getStringValue(
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

        if (primary != null
                && !String.valueOf(primary)
                .isBlank()) {

            return String.valueOf(
                    primary
            ).trim();
        }

        if (secondaryKey != null) {

            Object secondary =
                    context.get(
                            secondaryKey
                    );

            if (secondary != null
                    && !String.valueOf(secondary)
                    .isBlank()) {

                return String.valueOf(
                        secondary
                ).trim();
            }
        }

        return defaultValue;
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