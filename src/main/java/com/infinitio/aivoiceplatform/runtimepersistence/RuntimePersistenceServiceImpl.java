package com.infinitio.aivoiceplatform.runtimepersistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationRequestDto;
import com.infinitio.aivoiceplatform.llm.dto.runtime.LlmGenerationResponseDto;
import com.infinitio.aivoiceplatform.llm.entity.LlmInteraction;
import com.infinitio.aivoiceplatform.llm.repository.LlmInteractionRepository;
import com.infinitio.aivoiceplatform.stt.config.SttProperties;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionRequest;
import com.infinitio.aivoiceplatform.stt.dto.runtime.SttTranscriptionResponse;
import com.infinitio.aivoiceplatform.stt.entity.SttInteraction;
import com.infinitio.aivoiceplatform.stt.repository.SttInteractionRepository;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptArtifactService;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.entity.TtsInteraction;
import com.infinitio.aivoiceplatform.tts.repository.TtsInteractionRepository;
import com.infinitio.aivoiceplatform.user.constant.UserConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuntimePersistenceServiceImpl
        implements RuntimePersistenceService {

    private static final String SUCCESS = "SUCCESS";

    private final CallRepository callRepository;

    private final SttInteractionRepository
            sttInteractionRepository;

    private final SttProperties sttProperties;

    private final LlmInteractionRepository
            llmInteractionRepository;

    private final TtsInteractionRepository
            ttsInteractionRepository;

    private final TranscriptArtifactService
            transcriptArtifactService;

    private final ObjectMapper objectMapper;

    private final CurrentUserService
            currentUserService;


    // =========================================================
    // STT
    // =========================================================

    @Override
    @Transactional
    public void saveStt(
            SttTranscriptionRequest request,
            SttTranscriptionResponse response) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "STT request cannot be null."
            );
        }

        if (response == null) {
            throw new IllegalArgumentException(
                    "STT response cannot be null."
            );
        }

        String callPublicId =
                request.getCallId();

        Long createdBy =
                resolveCreatedBy(
                        callPublicId
                );

        SttInteraction interaction =
                SttInteraction.builder()
                        .callPublicId(
                                callPublicId
                        )
                        .transcript(
                                response.getTranscript()
                        )
                        .language(
                                response.getLanguage()
                        )
                        .provider(
                                response.getProvider()
                        )
                        .model(
                                sttProperties.getModel()
                        )
                        .finalTranscript(
                                response.isFinalTranscript()
                        )
                        .languageProbability(
                                response.getLanguageProbability()
                        )
                        .latencyMs(
                                response.getLatencyMs()
                        )
                        .audioSizeBytes(
                                request.getAudio() == null
                                        ? 0L
                                        : (long) request
                                        .getAudio()
                                        .length
                        )
                        .status(
                                SUCCESS
                        )
                        .createdBy(
                                createdBy
                        )
                        .build();

        SttInteraction saved =
                sttInteractionRepository.save(
                        interaction
                );

        log.info(
                "STT interaction persisted. " +
                        "interactionPublicId={}, callPublicId={}",
                saved.getPublicId(),
                callPublicId
        );

        /*
         * Store the user message in the single conversation
         * artifact for this call.
         *
         * IMPORTANT:
         * This method no longer creates a Transcript DB row.
         * The final Transcript DB record is created only after
         * the call and recording are completely available.
         */
        appendTranscript(
                callPublicId,
                "USER",
                response.getTranscript(),
                response.getLanguage(),
                "STT"
        );
    }


    // =========================================================
    // LLM
    // =========================================================

    @Override
    @Transactional
    public void saveLlm(
            LlmGenerationRequestDto request,
            LlmGenerationResponseDto response) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "LLM request cannot be null."
            );
        }

        if (response == null) {
            throw new IllegalArgumentException(
                    "LLM response cannot be null."
            );
        }

        String callPublicId =
                request.getCallId();

        Long createdBy =
                resolveCreatedBy(
                        callPublicId
                );

        LlmInteraction interaction =
                LlmInteraction.builder()
                        .callPublicId(
                                callPublicId
                        )
                        .requestMessages(
                                serializeMessages(
                                        request.getMessages()
                                )
                        )
                        .responseContent(
                                response.getContent()
                        )
                        .language(
                                response.getLanguage()
                        )
                        .provider(
                                response.getProvider()
                        )
                        .model(
                                response.getModel()
                        )
                        .finalResponse(
                                response.isFinalResponse()
                        )
                        .latencyMs(
                                response.getLatencyMs()
                        )
                        .inputTokens(
                                response.getInputTokens()
                        )
                        .outputTokens(
                                response.getOutputTokens()
                        )
                        .totalTokens(
                                response.getTotalTokens()
                        )
                        .providerRequestId(
                                response.getProviderRequestId()
                        )
                        .status(
                                SUCCESS
                        )
                        .createdBy(
                                createdBy
                        )
                        .build();

        LlmInteraction saved =
                llmInteractionRepository.save(
                        interaction
                );

        log.info(
                "LLM interaction persisted. " +
                        "interactionPublicId={}, callPublicId={}",
                saved.getPublicId(),
                callPublicId
        );

        /*
         * Store the assistant message in the same conversation
         * artifact used by STT.
         *
         * IMPORTANT:
         * No Transcript DB row is created here.
         */
        appendTranscript(
                callPublicId,
                "ASSISTANT",
                response.getContent(),
                response.getLanguage(),
                "LLM"
        );
    }


    // =========================================================
    // TTS
    // =========================================================

    @Override
    @Transactional
    public void saveTts(
            TtsSynthesisRequest request,
            TtsSynthesisResponse response) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "TTS request cannot be null."
            );
        }

        if (response == null) {
            throw new IllegalArgumentException(
                    "TTS response cannot be null."
            );
        }

        String callPublicId =
                request.getCallId();

        Long createdBy =
                resolveCreatedBy(
                        callPublicId
                );

        Long audioSizeBytes =
                resolveFileSize(
                        response.getFilePath()
                );

        TtsInteraction interaction =
                TtsInteraction.builder()
                        .callPublicId(
                                callPublicId
                        )
                        .text(
                                request.getText()
                        )
                        .language(
                                response.getLanguage()
                        )
                        .speaker(
                                response.getSpeaker()
                        )
                        .provider(
                                response.getProvider()
                        )
                        .model(
                                response.getModel()
                        )
                        .finalResponse(
                                response.isFinalResponse()
                        )
                        .latencyMs(
                                response.getLatencyMs()
                        )
                        .inputCharacters(
                                response.getInputCharacters()
                        )
                        .fileName(
                                response.getFileName()
                        )
                        .filePath(
                                response.getFilePath()
                        )
                        .audioUrl(
                                response.getAudioUrl()
                        )
                        .contentType(
                                response.getContentType()
                        )
                        .audioSizeBytes(
                                audioSizeBytes
                        )
                        .providerRequestId(
                                response.getProviderRequestId()
                        )
                        .status(
                                SUCCESS
                        )
                        .createdBy(
                                createdBy
                        )
                        .build();

        TtsInteraction saved =
                ttsInteractionRepository.save(
                        interaction
                );

        log.info(
                "TTS interaction persisted. " +
                        "interactionPublicId={}, callPublicId={}, " +
                        "filePath={}",
                saved.getPublicId(),
                callPublicId,
                response.getFilePath()
        );
    }


    // =========================================================
    // TRANSCRIPT ARTIFACT
    // =========================================================

    /**
     * Appends one conversation message to the single
     * transcript artifact associated with the call.
     *
     * <p>
     * This method intentionally does not create a Transcript
     * database row. The complete transcript database record is
     * created only after the call has ended and the call recording
     * has been persisted.
     * </p>
     *
     * @param callPublicId call public identifier
     * @param speakerType speaker type
     * @param text transcript text
     * @param language detected language
     * @param source source of transcript
     */
    private void appendTranscript(
            String callPublicId,
            String speakerType,
            String text,
            String language,
            String source) {

        if (text == null
                || text.isBlank()) {

            log.debug(
                    "Transcript artifact append skipped because " +
                            "text is empty. callPublicId={}, " +
                            "speakerType={}",
                    callPublicId,
                    speakerType
            );

            return;
        }

        if (callPublicId == null
                || callPublicId.isBlank()) {

            log.warn(
                    "Transcript artifact append skipped because " +
                            "callPublicId is missing. speakerType={}",
                    speakerType
            );

            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        /*
         * Sequence number belongs to the conversation artifact,
         * not to individual Transcript database rows.
         *
         * TranscriptArtifactService is responsible for maintaining
         * the message sequence inside the single JSON.GZ file.
         */
        Map<String, Object> message =
                new LinkedHashMap<>();

        message.put(
                "speakerType",
                speakerType
        );

        message.put(
                "text",
                text
        );

        message.put(
                "language",
                language
        );

        message.put(
                "source",
                source
        );

        message.put(
                "timestamp",
                now
        );

        String filePath =
                transcriptArtifactService.append(
                        callPublicId,
                        message
                );

        Call call =
                findCall(
                        callPublicId
                );

        /*
         * Synthetic Swagger/flow tests may not have a Call record.
         * The transcript artifact must still be persisted.
         */
        if (call == null) {

            log.info(
                    "No Call entity found. Transcript artifact " +
                            "stored as JSON.GZ only. " +
                            "callPublicId={}",
                    callPublicId
            );

            return;
        }

        /*
         * Store the path of the single complete conversation
         * artifact on the Call.
         *
         * The same path is updated for every message because
         * all messages belong to the same JSON.GZ file.
         */
        call.setTranscriptFilePath(
                filePath
        );

        callRepository.save(
                call
        );

        log.debug(
                "Transcript message appended to complete " +
                        "conversation artifact. " +
                        "callPublicId={}, speakerType={}, " +
                        "source={}, filePath={}",
                callPublicId,
                speakerType,
                source,
                filePath
        );
    }


    // =========================================================
    // CALL
    // =========================================================

    /**
     * Finds the Call entity using its public identifier.
     *
     * @param callPublicId call public identifier
     * @return Call entity or null when not available
     */
    private Call findCall(
            String callPublicId) {

        if (callPublicId == null
                || callPublicId.isBlank()) {

            return null;
        }

        return callRepository
                .findByPublicId(
                        callPublicId
                )
                .orElse(null);
    }


    // =========================================================
    // CREATED BY
    // =========================================================

    /**
     * Resolves the user responsible for runtime persistence.
     *
     * <p>
     * For a real call, the Call creator is preferred. If no Call
     * exists, the authenticated user is used. Background provider
     * callbacks fall back to the configured system user.
     * </p>
     *
     * @param callPublicId call public identifier
     * @return user identifier
     */
    private Long resolveCreatedBy(
            String callPublicId) {

        /*
         * Real call:
         * use the user who created the Call.
         */
        Call call =
                findCall(
                        callPublicId
                );

        if (call != null
                && call.getCreatedBy() != null) {

            return call.getCreatedBy();
        }

        /*
         * Swagger/API authenticated request.
         */
        try {

            if (currentUserService.isAuthenticated()) {

                Long currentUserId =
                        currentUserService
                                .getCurrentUserId();

                if (currentUserId != null) {

                    return currentUserId;
                }
            }

        } catch (Exception exception) {

            log.debug(
                    "Unable to resolve authenticated user " +
                            "for runtime persistence. " +
                            "Using system user.",
                    exception
            );
        }

        /*
         * Background provider callback.
         *
         * Use the same system user constant used by the rest
         * of the application. Do not use a separate hardcoded
         * value here.
         */
        log.debug(
                "No authenticated user available for runtime " +
                        "persistence. Using system user. " +
                        "systemUserId={}",
                UserConstants.SYSTEM_USER_ID
        );

        return UserConstants.SYSTEM_USER_ID;
    }


    // =========================================================
    // JSON SERIALIZATION
    // =========================================================

    /**
     * Serializes LLM messages for persistence.
     *
     * @param messages LLM request messages
     * @return JSON representation
     */
    private String serializeMessages(
            List<?> messages) {

        try {

            return objectMapper.writeValueAsString(
                    messages
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to serialize LLM request messages.",
                    exception
            );
        }
    }


    // =========================================================
    // FILE SIZE
    // =========================================================

    /**
     * Resolves the size of a persisted audio file.
     *
     * @param filePath audio file path
     * @return file size in bytes or null
     */
    private Long resolveFileSize(
            String filePath) {

        if (filePath == null
                || filePath.isBlank()) {

            return null;
        }

        try {

            Path path =
                    Paths.get(
                            filePath
                    );

            if (!Files.exists(path)) {

                return null;
            }

            return Files.size(
                    path
            );

        } catch (Exception exception) {

            log.warn(
                    "Unable to determine file size. path={}",
                    filePath,
                    exception
            );

            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveTranscriptMessage(
            String callPublicId,
            String speakerType,
            String text,
            String language,
            String source) {

        if (callPublicId == null
                || callPublicId.isBlank()) {

            log.warn(
                    "Transcript message persistence skipped because " +
                            "callPublicId is missing. speakerType={}",
                    speakerType
            );

            return;
        }

        if (text == null
                || text.isBlank()) {

            log.debug(
                    "Transcript message persistence skipped because " +
                            "text is empty. callPublicId={}, speakerType={}",
                    callPublicId,
                    speakerType
            );

            return;
        }

        appendTranscript(
                callPublicId,
                speakerType,
                text,
                language,
                source
        );

        log.info(
                "Conversation transcript message persisted. " +
                        "callPublicId={}, speakerType={}, source={}",
                callPublicId,
                speakerType,
                source
        );
    }
}