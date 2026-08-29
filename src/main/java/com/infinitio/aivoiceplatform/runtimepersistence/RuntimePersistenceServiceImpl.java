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
import com.infinitio.aivoiceplatform.transcript.entity.Transcript;
import com.infinitio.aivoiceplatform.transcript.repository.TranscriptRepository;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptArtifactService;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisRequest;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsSynthesisResponse;
import com.infinitio.aivoiceplatform.tts.entity.TtsInteraction;
import com.infinitio.aivoiceplatform.tts.repository.TtsInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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

    /**
     * Used only when runtime execution happens without
     * an authenticated user and without an existing Call.
     *
     * Prefer the actual authenticated user or Call.createdBy.
     */
    private static final Long SYSTEM_USER_ID = 1L;

    private static final String SUCCESS = "SUCCESS";

    private final CallRepository callRepository;

    private final SttInteractionRepository
            sttInteractionRepository;

    private final SttProperties sttProperties;

    private final LlmInteractionRepository
            llmInteractionRepository;

    private final TtsInteractionRepository
            ttsInteractionRepository;

    private final TranscriptRepository
            transcriptRepository;

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

        appendTranscript(
                callPublicId,
                "USER",
                response.getTranscript(),
                response.getLanguage(),
                "STT",
                createdBy
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

        appendTranscript(
                callPublicId,
                "ASSISTANT",
                response.getContent(),
                response.getLanguage(),
                "LLM",
                createdBy
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
    // TRANSCRIPT
    // =========================================================

    private void appendTranscript(
            String callPublicId,
            String speakerType,
            String text,
            String language,
            String source,
            Long createdBy) {

        if (text == null
                || text.isBlank()) {

            return;
        }

        if (callPublicId == null
                || callPublicId.isBlank()) {

            log.warn(
                    "Transcript persistence skipped because " +
                            "callPublicId is missing."
            );

            return;
        }

        LocalDateTime now =
                LocalDateTime.now();

        int sequence =
                nextSequence(
                        callPublicId
                );

        Map<String, Object> message =
                new LinkedHashMap<>();

        message.put(
                "sequenceNumber",
                sequence
        );

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
         * Swagger synthetic tests do not have a Call record.
         * In that case the JSON.GZ artifact is still persisted.
         */
        if (call == null) {

            log.info(
                    "No Call entity found. Transcript stored " +
                            "as JSON.GZ only. callPublicId={}",
                    callPublicId
            );

            return;
        }

        Transcript transcript =
                Transcript.builder()
                        .call(
                                call
                        )
                        .sequenceNumber(
                                sequence
                        )
                        .speakerType(
                                speakerType
                        )
                        .text(
                                text
                        )
                        .language(
                                language
                        )
                        .source(
                                source
                        )
                        .startedAt(
                                now
                        )
                        .endedAt(
                                now
                        )
                        .createdBy(
                                createdBy
                        )
                        .build();

        transcriptRepository.save(
                transcript
        );

        /*
         * Store the latest transcript artifact path
         * on the actual Call.
         */
        call.setTranscriptFilePath(
                filePath
        );

        callRepository.save(
                call
        );
    }


    // =========================================================
    // SEQUENCE
    // =========================================================

    private int nextSequence(
            String callPublicId) {

        Call call =
                findCall(
                        callPublicId
                );

        if (call == null) {

            /*
             * Synthetic Flow test.
             */
            return 1;
        }

        List<Transcript> latest =
                transcriptRepository
                        .findByCallIdOrderBySequenceNumberDesc(
                                call.getId(),
                                PageRequest.of(
                                        0,
                                        1
                                )
                        )
                        .getContent();

        if (latest.isEmpty()
                || latest.get(0)
                .getSequenceNumber() == null) {

            return 1;
        }

        return latest.get(0)
                .getSequenceNumber()
                + 1;
    }


    // =========================================================
    // CALL
    // =========================================================

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
                            "for runtime persistence.",
                    exception
            );
        }

        /*
         * Background provider callback.
         */
        return SYSTEM_USER_ID;
    }


    // =========================================================
    // JSON SERIALIZATION
    // =========================================================

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


}