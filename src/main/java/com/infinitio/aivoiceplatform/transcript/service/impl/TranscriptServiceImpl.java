package com.infinitio.aivoiceplatform.transcript.service.impl;

import java.util.List;

import com.infinitio.aivoiceplatform.transcript.repository.TranscriptArtifactRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;
import com.infinitio.aivoiceplatform.transcript.entity.TranscriptArtifact;
import com.infinitio.aivoiceplatform.transcript.repository.TranscriptArtifactRepository;
import com.infinitio.aivoiceplatform.transcript.dto.response.CallTranscriptMessageResponse;
import com.infinitio.aivoiceplatform.transcript.dto.response.CallTranscriptResponse;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.transcript.constant.TranscriptConstants;
import com.infinitio.aivoiceplatform.transcript.constant.TranscriptMessages;
import com.infinitio.aivoiceplatform.transcript.dto.request.CreateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.request.UpdateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.response.TranscriptResponse;
import com.infinitio.aivoiceplatform.transcript.entity.Transcript;
import com.infinitio.aivoiceplatform.transcript.mapper.TranscriptMapper;
import com.infinitio.aivoiceplatform.transcript.repository.TranscriptRepository;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptService;
import com.infinitio.aivoiceplatform.transcript.validator.TranscriptValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.infinitio.aivoiceplatform.callsession.dto.CallConversationMessageDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.storage.ConversationStorageService;

import com.infinitio.aivoiceplatform.callrecording.entity.CallRecording;
import com.infinitio.aivoiceplatform.callrecording.repository.CallRecordingRepository;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptArtifactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for Transcript operations.
 *
 * <p>
 * Transcript records are persisted in MySQL and are associated
 * directly with a Call.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TranscriptServiceImpl
        implements TranscriptService {

    private static final Integer ACTIVE = 1;

    private static final Integer DELETED = 1;

    private static final Integer NOT_DELETED = 0;

    private final TranscriptRepository transcriptRepository;

    private final CallRepository callRepository;

    private final TranscriptMapper transcriptMapper;

    private final TranscriptValidator transcriptValidator;

    private static final String COMPLETE_RUNTIME_SOURCE =
            "RUNTIME_COMPLETE";

    private static final String COMPLETE_CONVERSATION_SPEAKER =
            "CONVERSATION";

    private final CallRecordingRepository
            callRecordingRepository;

    private final TranscriptArtifactService
            transcriptArtifactService;

    private final TranscriptArtifactRepository
            transcriptArtifactRepository;

    private final CallSessionRepository
            callSessionRepository;

    private final ConversationStorageService
            conversationStorageService;

    /**
     * {@inheritDoc}
     */
    @Override
    public TranscriptResponse create(
            CreateTranscriptRequest request) {

        log.info(
                "Creating transcript. callPublicId={}, sequenceNumber={}",
                request != null
                        ? request.getCallPublicId()
                        : null,
                request != null
                        ? request.getSequenceNumber()
                        : null
        );

        transcriptValidator.validateCreate(
                request
        );

        Call call =
                getCall(
                        request.getCallPublicId()
                );

        validateSequenceNumber(
                call.getId(),
                request.getSequenceNumber(),
                null
        );

        Transcript transcript =
                transcriptMapper.toEntity(
                        request,
                        call
                );

        Transcript savedTranscript =
                transcriptRepository.save(
                        transcript
                );

        log.info(
                "Transcript created successfully. publicId={}, callPublicId={}, sequenceNumber={}",
                savedTranscript.getPublicId(),
                call.getPublicId(),
                savedTranscript.getSequenceNumber()
        );

        return transcriptMapper.toResponse(
                savedTranscript
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TranscriptResponse getByPublicId(
            String publicId) {

        log.debug(
                "Fetching transcript. publicId={}",
                publicId
        );

        Transcript transcript =
                getActiveTranscript(
                        publicId
                );

        return transcriptMapper.toResponse(
                transcript
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TranscriptResponse> getByCallPublicId(
            String callPublicId) {

        log.debug(
                "Fetching transcripts by call. callPublicId={}",
                callPublicId
        );

        if (callPublicId == null
                || callPublicId.isBlank()) {

            throw new ResourceNotFoundException(
                    TranscriptMessages.CALL_NOT_FOUND
            );
        }

        Call call =
                getCall(
                        callPublicId
                );

        return transcriptRepository
                .findByCallIdOrderBySequenceNumberAsc(
                        call.getId(),
                        PageRequest.of(
                                TranscriptConstants.DEFAULT_PAGE,
                                TranscriptConstants.MAX_PAGE_SIZE
                        )
                )
                .getContent()
                .stream()
                .filter(transcript ->
                        NOT_DELETED.equals(
                                transcript.getIsDeleted()
                        )
                )
                .map(transcriptMapper::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TranscriptResponse update(
            String publicId,
            UpdateTranscriptRequest request) {

        log.info(
                "Updating transcript. publicId={}",
                publicId
        );

        transcriptValidator.validateUpdate(
                request
        );

        Transcript transcript =
                getActiveTranscript(
                        publicId
                );

        if (request.getSequenceNumber() != null
                && !request.getSequenceNumber()
                .equals(transcript.getSequenceNumber())) {

            validateSequenceNumber(
                    transcript.getCall().getId(),
                    request.getSequenceNumber(),
                    transcript.getId()
            );
        }

        transcriptMapper.updateEntity(
                request,
                transcript
        );

        Transcript savedTranscript =
                transcriptRepository.save(
                        transcript
                );

        log.info(
                "Transcript updated successfully. publicId={}",
                savedTranscript.getPublicId()
        );

        return transcriptMapper.toResponse(
                savedTranscript
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting transcript. publicId={}",
                publicId
        );

        Transcript transcript =
                getActiveTranscript(
                        publicId
                );

        transcript.setIsDeleted(
                DELETED
        );

        transcript.setIsActive(
                0
        );

        transcriptRepository.save(
                transcript
        );

        log.info(
                "Transcript deleted successfully. publicId={}",
                publicId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating transcript. publicId={}",
                publicId
        );

        Transcript transcript =
                getTranscript(
                        publicId
                );

        transcript.setIsActive(
                ACTIVE
        );

        transcript.setIsDeleted(
                NOT_DELETED
        );

        transcriptRepository.save(
                transcript
        );

        log.info(
                "Transcript activated successfully. publicId={}",
                publicId
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating transcript. publicId={}",
                publicId
        );

        Transcript transcript =
                getActiveTranscript(
                        publicId
                );

        transcript.setIsActive(
                0
        );

        transcriptRepository.save(
                transcript
        );

        log.info(
                "Transcript deactivated successfully. publicId={}",
                publicId
        );
    }

    /**
     * Retrieves a Call using its public identifier.
     *
     * @param callPublicId call public identifier
     * @return call entity
     */
    private Call getCall(
            String callPublicId) {

        return callRepository
                .findByPublicId(
                        callPublicId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                TranscriptMessages.CALL_NOT_FOUND
                        )
                );
    }

    /**
     * Retrieves an existing transcript.
     *
     * @param publicId transcript public identifier
     * @return transcript entity
     */
    private Transcript getTranscript(
            String publicId) {

        return transcriptRepository
                .findByPublicId(
                        publicId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                TranscriptMessages.NOT_FOUND
                        )
                );
    }

    /**
     * Retrieves an active, non-deleted transcript.
     *
     * @param publicId transcript public identifier
     * @return transcript entity
     */
    private Transcript getActiveTranscript(
            String publicId) {

        Transcript transcript =
                getTranscript(
                        publicId
                );

        if (!NOT_DELETED.equals(
                transcript.getIsDeleted())) {

            throw new ResourceNotFoundException(
                    TranscriptMessages.NOT_FOUND
            );
        }

        if (!ACTIVE.equals(
                transcript.getIsActive())) {

            throw new ResourceNotFoundException(
                    TranscriptMessages.NOT_FOUND
            );
        }

        return transcript;
    }

    /**
     * Validates transcript sequence uniqueness within a call.
     *
     * @param callId database identifier of the call
     * @param sequenceNumber sequence number
     * @param transcriptId current transcript database identifier
     */
    private void validateSequenceNumber(
            Long callId,
            Integer sequenceNumber,
            Long transcriptId) {

        boolean exists =
                transcriptRepository
                        .existsByCallIdAndSequenceNumber(
                                callId,
                                sequenceNumber
                        );

        if (!exists) {
            return;
        }

        if (transcriptId != null) {

            Transcript existingTranscript =
                    transcriptRepository
                            .findByCallIdOrderBySequenceNumberAsc(
                                    callId,
                                    PageRequest.of(
                                            TranscriptConstants.DEFAULT_PAGE,
                                            TranscriptConstants.MAX_PAGE_SIZE
                                    )
                            )
                            .getContent()
                            .stream()
                            .filter(transcript ->
                                    sequenceNumber.equals(
                                            transcript.getSequenceNumber()
                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (existingTranscript == null
                    || transcriptId.equals(
                    existingTranscript.getId()
            )) {

                return;
            }
        }

        throw new ConflictException(
                TranscriptMessages
                        .SEQUENCE_NUMBER_ALREADY_EXISTS
        );
    }

    /**
     * {@inheritDoc}
     */
    /**
     * Finalizes the complete transcript for a call.
     *
     * <p>
     * The runtime transcript artifact is the primary source of truth
     * because it receives every final USER and ASSISTANT message during
     * the live conversation. The CallSession conversation storage is
     * used only as a backward-compatible fallback for calls created
     * before runtime transcript artifact persistence was enabled.
     * </p>
     *
     * @param callPublicId call public identifier
     * @param callRecordingPublicId call recording public identifier
     * @return finalized transcript response
     */
    @Override
    public TranscriptResponse finalizeCallTranscript(
            String callPublicId,
            String callRecordingPublicId) {

        log.info(
                "Finalizing complete call transcript. " +
                        "callPublicId={}, callRecordingPublicId={}",
                callPublicId,
                callRecordingPublicId
        );

        Call call =
                getCall(
                        callPublicId
                );

        CallSession callSession =
                callSessionRepository
                        .findByCallIdAndIsDeleted(
                                callPublicId,
                                NOT_DELETED
                        )
                        .orElse(null);

        CallRecording callRecording =
                callRecordingRepository
                        .findByPublicId(
                                callRecordingPublicId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Call recording not found."
                                )
                        );

        if (callRecording.getCall() == null
                || !call.getId().equals(
                callRecording.getCall().getId()
        )) {

            throw new IllegalArgumentException(
                    "Call recording does not belong to the call."
            );
        }

        /*
         * The runtime transcript artifact is the primary source of
         * truth for the complete conversation.
         *
         * Every final USER transcript is appended by the Voice Gateway
         * and every final ASSISTANT response is appended by the LLM
         * runtime persistence layer.
         */
        List<Map<String, Object>> messages =
                transcriptArtifactService.readMessages(
                        callPublicId
                );

        /*
         * Backward-compatible fallback.
         *
         * Existing calls created before the runtime transcript artifact
         * was available may still have their conversation in the
         * CallSession conversation storage.
         */
        if (messages.isEmpty()) {

            log.warn(
                    "Transcript artifact contains no messages. " +
                            "Falling back to CallSession conversation storage. " +
                            "callPublicId={}",
                    callPublicId
            );

            messages =
                    readConversationStorageMessages(
                            callPublicId
                    );
        }

        /*
         * Remove invalid/empty messages while preserving the exact
         * runtime conversation order.
         */
        List<Map<String, Object>> validMessages =
                new ArrayList<>();

        for (Map<String, Object> message :
                messages) {

            if (message == null) {
                continue;
            }

            Object textValue =
                    message.get(
                            "text"
                    );

            if (textValue == null
                    || String.valueOf(
                    textValue
            ).isBlank()) {

                continue;
            }

            Map<String, Object> normalizedMessage =
                    new LinkedHashMap<>(
                            message
                    );

            normalizedMessage.put(
                    "speakerType",
                    normalizeSpeakerType(
                            String.valueOf(
                                    message.getOrDefault(
                                            "speakerType",
                                            "UNKNOWN"
                                    )
                            )
                    )
            );

            validMessages.add(
                    normalizedMessage
            );
        }

        if (validMessages.isEmpty()) {

            log.warn(
                    "No conversation messages found for completed call. " +
                            "callPublicId={}",
                    callPublicId
            );

            return null;
        }

        log.info(
                "Complete conversation loaded for transcript finalization. " +
                        "callPublicId={}, messageCount={}",
                callPublicId,
                validMessages.size()
        );

        String completeText =
                buildCompleteTranscriptText(
                        validMessages
                );

        String language =
                resolveLanguage(
                        validMessages
                );

        /*
         * Use the CallSession language only when the runtime
         * transcript artifact does not contain a language.
         */
        if (language == null
                || language.isBlank()) {

            language =
                    callSession.getLanguage();
        }

        Transcript transcript =
                transcriptRepository
                        .findFirstByCallIdAndSource(
                                call.getId(),
                                COMPLETE_RUNTIME_SOURCE
                        )
                        .orElse(null);

        if (transcript == null) {

            transcript =
                    Transcript.builder()
                            .call(call)
                            .callRecording(callRecording)
                            .sequenceNumber(1)
                            .speakerType(
                                    COMPLETE_CONVERSATION_SPEAKER
                            )
                            .text(completeText)
                            .language(language)
                            .source(
                                    COMPLETE_RUNTIME_SOURCE
                            )
                            .startedAt(
                                    call.getStartedAt()
                            )
                            .endedAt(
                                    call.getEndedAt()
                            )
                            .createdBy(
                                    call.getCreatedBy()
                            )
                            .build();

        } else {

            transcript.setCallRecording(
                    callRecording
            );

            transcript.setText(
                    completeText
            );

            transcript.setLanguage(
                    language
            );

            transcript.setStartedAt(
                    call.getStartedAt()
            );

            transcript.setEndedAt(
                    call.getEndedAt()
            );
        }

        Transcript savedTranscript =
                transcriptRepository.save(
                        transcript
                );

        log.info(
                "Complete call transcript finalized successfully. " +
                        "transcriptPublicId={}, callPublicId={}, " +
                        "recordingPublicId={}, messageCount={}",
                savedTranscript.getPublicId(),
                callPublicId,
                callRecordingPublicId,
                validMessages.size()
        );

        return transcriptMapper.toResponse(
                savedTranscript
        );
    }

    /**
     * Reads conversation messages from the legacy CallSession
     * conversation storage.
     *
     * <p>
     * This method exists only as a backward-compatible fallback.
     * New calls should normally be read from the runtime transcript
     * artifact.
     * </p>
     *
     * @param callPublicId call public identifier
     * @return conversation messages
     */
    private List<Map<String, Object>> readConversationStorageMessages(
            String callPublicId) {

        CallSession callSession =
                callSessionRepository
                        .findByCallIdAndIsDeleted(
                                callPublicId,
                                NOT_DELETED
                        )
                        .orElse(null);

        if (callSession == null) {

            log.warn(
                    "Call session not found while reading fallback " +
                            "conversation storage. callPublicId={}",
                    callPublicId
            );

            return new ArrayList<>();
        }

        String storageKey =
                callSession.getConversationStorageKey();

        if (storageKey == null
                || storageKey.isBlank()) {

            log.warn(
                    "Conversation storage key is not available. " +
                            "callPublicId={}",
                    callPublicId
            );

            return new ArrayList<>();
        }

        List<CallConversationMessageDto> conversationMessages =
                conversationStorageService.readMessages(
                        storageKey
                );

        List<Map<String, Object>> messages =
                new ArrayList<>();

        for (CallConversationMessageDto message :
                conversationMessages) {

            if (message == null
                    || message.getText() == null
                    || message.getText().isBlank()) {

                continue;
            }

            Map<String, Object> transcriptMessage =
                    new LinkedHashMap<>();

            transcriptMessage.put(
                    "speakerType",
                    normalizeSpeakerType(
                            message.getRole()
                    )
            );

            transcriptMessage.put(
                    "text",
                    message.getText()
            );

            transcriptMessage.put(
                    "language",
                    callSession.getLanguage()
            );

            transcriptMessage.put(
                    "source",
                    "CALL_SESSION_FALLBACK"
            );

            transcriptMessage.put(
                    "timestamp",
                    message.getTimestamp()
            );

            transcriptMessage.put(
                    "sequenceNumber",
                    messages.size() + 1
            );

            messages.add(
                    transcriptMessage
            );
        }

        log.info(
                "Fallback conversation storage loaded. " +
                        "callPublicId={}, messageCount={}",
                callPublicId,
                messages.size()
        );

        return messages;
    }

    /**
     * Builds one complete conversation text from all
     * transcript artifact messages.
     *
     * @param messages transcript messages
     * @return complete conversation text
     */
    private String buildCompleteTranscriptText(
            List<Map<String, Object>> messages) {

        StringBuilder transcriptText =
                new StringBuilder();

        for (Map<String, Object> message :
                messages) {

            String speaker =
                    String.valueOf(
                            message.getOrDefault(
                                    "speakerType",
                                    "UNKNOWN"
                            )
                    );

            String text =
                    String.valueOf(
                            message.getOrDefault(
                                    "text",
                                    ""
                            )
                    );

            if (text.isBlank()) {
                continue;
            }

            if (transcriptText.length() > 0) {

                transcriptText.append(
                        System.lineSeparator()
                );

                transcriptText.append(
                        System.lineSeparator()
                );
            }

            transcriptText
                    .append(speaker)
                    .append(": ")
                    .append(text);
        }

        return transcriptText.toString();
    }

    /**
     * Resolves the first available language from transcript messages.
     *
     * @param messages transcript messages
     * @return language when available
     */
    private String resolveLanguage(
            List<Map<String, Object>> messages) {

        for (Map<String, Object> message :
                messages) {

            Object language =
                    message.get(
                            "language"
                    );

            if (language != null
                    && !String.valueOf(
                    language
            ).isBlank()) {

                return String.valueOf(
                        language
                );
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    /**
     * {@inheritDoc}
     */
    /**
     * Retrieves the complete conversation transcript for a call.
     *
     * <p>
     * The runtime transcript artifact is used as the primary source
     * because it contains the complete sequence of final USER and
     * ASSISTANT messages captured during the live call.
     * </p>
     *
     * <p>
     * CallSession conversation storage is used only as a fallback
     * for older calls whose runtime transcript artifact is unavailable.
     * </p>
     *
     * @param callPublicId call public identifier
     * @return complete call transcript
     */
    @Override
    @Transactional(readOnly = true)
    public CallTranscriptResponse getCompleteCallTranscript(
            String callPublicId) {

        log.info(
                "Fetching complete call transcript. callPublicId={}",
                callPublicId
        );

        /*
         * Validate that the Call exists.
         */
        getCall(
                callPublicId
        );

        /*
         * Primary source:
         *
         * Runtime transcript artifact.
         *
         * This contains the complete live conversation:
         *
         * USER
         * ASSISTANT
         * USER
         * ASSISTANT
         * USER
         * ASSISTANT
         */
        List<Map<String, Object>> messages =
                transcriptArtifactService.readMessages(
                        callPublicId
                );

        /*
         * Backward-compatible fallback for older calls.
         */
        if (messages.isEmpty()) {

            log.warn(
                    "Runtime transcript artifact is empty. " +
                            "Using CallSession conversation storage fallback. " +
                            "callPublicId={}",
                    callPublicId
            );

            messages =
                    readConversationStorageMessages(
                            callPublicId
                    );
        }

        List<CallTranscriptMessageResponse>
                responseMessages =
                new ArrayList<>();

        int sequenceNumber = 1;

        for (Map<String, Object> message :
                messages) {

            if (message == null) {
                continue;
            }

            Object textValue =
                    message.get(
                            "text"
                    );

            if (textValue == null
                    || String.valueOf(
                    textValue
            ).isBlank()) {

                continue;
            }

            Object speakerValue =
                    message.get(
                            "speakerType"
                    );

            Object languageValue =
                    message.get(
                            "language"
                    );

            Object sourceValue =
                    message.get(
                            "source"
                    );

            Object timestampValue =
                    message.get(
                            "timestamp"
                    );

            responseMessages.add(
                    CallTranscriptMessageResponse
                            .builder()
                            .sequenceNumber(
                                    sequenceNumber++
                            )
                            .speakerType(
                                    normalizeSpeakerType(
                                            speakerValue == null
                                                    ? null
                                                    : String.valueOf(
                                                    speakerValue
                                            )
                                    )
                            )
                            .text(
                                    String.valueOf(
                                            textValue
                                    )
                            )
                            .language(
                                    languageValue == null
                                            ? null
                                            : String.valueOf(
                                            languageValue
                                    )
                            )
                            .source(
                                    sourceValue == null
                                            ? null
                                            : String.valueOf(
                                            sourceValue
                                    )
                            )
                            .timestamp(
                                    timestampValue == null
                                            ? null
                                            : String.valueOf(
                                            timestampValue
                                    )
                            )
                            .build()
            );
        }

        /*
         * Runtime transcript artifact file name.
         */
        String fileName =
                callPublicId + ".json.gz";

        log.info(
                "Complete call transcript fetched successfully. " +
                        "callPublicId={}, messageCount={}, fileName={}",
                callPublicId,
                responseMessages.size(),
                fileName
        );

        return CallTranscriptResponse
                .builder()
                .callPublicId(
                        callPublicId
                )
                .available(
                        !responseMessages.isEmpty()
                )
                .fileName(
                        fileName
                )
                .sizeBytes(
                        null
                )
                .messages(
                        responseMessages
                )
                .build();
    }

    /**
     * Converts conversation role into the speaker type expected
     * by the Calls frontend.
     *
     * @param role conversation role
     * @return normalized speaker type
     */
    private String normalizeSpeakerType(
            String role) {

        if (role == null
                || role.isBlank()) {

            return "UNKNOWN";
        }

        if ("user".equalsIgnoreCase(role)) {

            return "USER";
        }

        if ("assistant".equalsIgnoreCase(role)) {

            return "ASSISTANT";
        }

        return role.toUpperCase();
    }

    /**
     * Extracts the file name from a conversation storage key.
     *
     * @param storageKey conversation storage key
     * @return file name
     */
    private String extractFileName(
            String storageKey) {

        if (storageKey == null
                || storageKey.isBlank()) {

            return null;
        }

        int separatorIndex =
                storageKey.lastIndexOf('/');

        if (separatorIndex < 0) {

            return storageKey;
        }

        return storageKey.substring(
                separatorIndex + 1
        );
    }
}