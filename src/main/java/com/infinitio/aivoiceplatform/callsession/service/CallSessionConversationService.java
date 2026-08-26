package com.infinitio.aivoiceplatform.callsession.service;

import com.infinitio.aivoiceplatform.callsession.dto.request.AddConversationMessageRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;

/**
 * Provides conversation history operations for a call session.
 *
 * <p>
 * Conversation messages are stored outside MySQL as local JSONL
 * files. Completed conversations are archived as GZIP-compressed
 * JSONL files.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallSessionConversationService {

    /**
     * Adds a conversation message to the call.
     *
     * @param callId public identifier of the call
     * @param request conversation message request
     * @return current call session response
     */
    CallSessionResponseDto addConversationMessage(
            String callId,
            AddConversationMessageRequestDto request
    );
}