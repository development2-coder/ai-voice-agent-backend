package com.infinitio.aivoiceplatform.callsession.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infinitio.aivoiceplatform.callsession.constant.CallSessionMessages;
import com.infinitio.aivoiceplatform.callsession.constant.CallSessionStatus;
import com.infinitio.aivoiceplatform.callsession.dto.CallConversationMessageDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.AddConversationMessageRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.entity.CallSession;
import com.infinitio.aivoiceplatform.callsession.mapper.CallSessionMapper;
import com.infinitio.aivoiceplatform.callsession.repository.CallSessionRepository;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionConversationService;
import com.infinitio.aivoiceplatform.callsession.storage.ConversationStorageService;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements conversation history business logic.
 *
 * <p>
 * Conversation messages are stored in local JSONL files.
 * MySQL stores only the conversation storage key and call-session
 * runtime metadata.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallSessionConversationServiceImpl
        implements CallSessionConversationService {

    private static final Integer NOT_DELETED = 0;

    private final CallSessionRepository
            callSessionRepository;

    private final CallSessionMapper
            callSessionMapper;

    private final ConversationStorageService
            conversationStorageService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CallSessionResponseDto addConversationMessage(
            String callId,
            AddConversationMessageRequestDto request) {

        log.info(
                "Adding conversation message. callId={}, role={}",
                callId,
                request != null
                        ? request.getRole()
                        : null
        );

        validateRequest(
                callId,
                request
        );

        CallSession callSession =
                callSessionRepository
                        .findByCallIdAndIsDeleted(
                                callId,
                                NOT_DELETED
                        )
                        .orElseThrow(() -> {

                            log.warn(
                                    "Call session not found while adding " +
                                            "conversation message. callId={}",
                                    callId
                            );

                            return new ResourceNotFoundException(
                                    CallSessionMessages
                                            .CALL_SESSION_NOT_FOUND
                            );
                        });

        if (CallSessionStatus.ENDED
                .equals(
                        callSession.getStatus()
                )) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CONVERSATION_NOT_ALLOWED_AFTER_END
            );
        }

        CallConversationMessageDto message =
                CallConversationMessageDto.builder()
                        .role(
                                request.getRole()
                        )
                        .text(
                                request.getText()
                        )
                        .timestamp(
                                Instant.now()
                        )
                        .build();

        String storageKey =
                conversationStorageService
                        .appendMessage(
                                callSession.getTenantId(),
                                callSession.getCallId(),
                                callSession.getConversationStorageKey(),
                                message
                        );

        callSession.setConversationStorageKey(
                storageKey
        );

        callSession.incrementTurnIndex();

        CallSession savedCallSession =
                callSessionRepository.save(
                        callSession
                );

        log.info(
                "Conversation message persisted successfully. " +
                        "callId={}, turnIndex={}, storageKey={}",
                savedCallSession.getCallId(),
                savedCallSession.getTurnIndex(),
                savedCallSession.getConversationStorageKey()
        );

        return callSessionMapper.toResponse(
                savedCallSession
        );
    }

    /**
     * Validates conversation message request.
     *
     * @param callId call-session public identifier
     * @param request conversation request
     */
    private void validateRequest(
            String callId,
            AddConversationMessageRequestDto request) {

        if (callId == null
                || callId.isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CALL_ID_REQUIRED
            );
        }

        if (request == null) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CONVERSATION_MESSAGE_REQUEST_REQUIRED
            );
        }

        if (request.getRole() == null
                || request.getRole().isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CONVERSATION_ROLE_REQUIRED
            );
        }

        if (request.getText() == null
                || request.getText().isBlank()) {

            throw new BadRequestException(
                    CallSessionMessages
                            .CONVERSATION_TEXT_REQUIRED
            );
        }
    }
}