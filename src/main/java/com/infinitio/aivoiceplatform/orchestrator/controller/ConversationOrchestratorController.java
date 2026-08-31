package com.infinitio.aivoiceplatform.orchestrator.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.orchestrator.constant.ConversationOrchestratorMessages;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.BargeInRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.EndConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessAudioRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessDtmfRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.ProcessTranscriptRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.request.StartConversationRequestDto;
import com.infinitio.aivoiceplatform.orchestrator.dto.response.ConversationOrchestratorResponseDto;
import com.infinitio.aivoiceplatform.orchestrator.service.ConversationOrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Conversation Orchestrator runtime operations.
 *
 * <p>
 * Provides endpoints used to start, process and terminate
 * AI voice conversations.
 * </p>
 *
 * <p>
 * The controller contains no business or orchestration logic.
 * All runtime processing is delegated to
 * {@link ConversationOrchestratorService}.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/conversations")
public class ConversationOrchestratorController {

    private final ConversationOrchestratorService
            conversationOrchestratorService;

    /**
     * Starts a new conversation.
     *
     * @param request conversation start request
     * @return conversation runtime response
     */
    @PostMapping("/start")
    public ResponseEntity<
            ApiResponse<ConversationOrchestratorResponseDto>>
    start(
            @Valid
            @RequestBody
            StartConversationRequestDto request) {

        ConversationOrchestratorResponseDto response =
                conversationOrchestratorService.start(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                ConversationOrchestratorMessages
                                        .CONVERSATION_STARTED,
                                response
                        )
                );
    }

    /**
     * Processes caller audio.
     *
     * @param request caller audio request
     * @return conversation runtime response
     */
    @PostMapping("/audio")
    public ResponseEntity<
            ApiResponse<ConversationOrchestratorResponseDto>>
    processAudio(
            @Valid
            @RequestBody
            ProcessAudioRequestDto request) {

        ConversationOrchestratorResponseDto response =
                conversationOrchestratorService.processAudio(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                ConversationOrchestratorMessages
                                        .CONVERSATION_AUDIO_PROCESSED,
                                response
                        )
                );
    }

    /**
     * Processes a caller transcript.
     *
     * @param request caller transcript request
     * @return conversation runtime response
     */
    @PostMapping("/transcript")
    public ResponseEntity<
            ApiResponse<ConversationOrchestratorResponseDto>>
    processTranscript(
            @Valid
            @RequestBody
            ProcessTranscriptRequestDto request) {

        ConversationOrchestratorResponseDto response =
                conversationOrchestratorService.processTranscript(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                ConversationOrchestratorMessages
                                        .CONVERSATION_TRANSCRIPT_PROCESSED,
                                response
                        )
                );
    }

    /**
     * Processes DTMF input.
     *
     * @param request DTMF request
     * @return conversation runtime response
     */
    @PostMapping("/dtmf")
    public ResponseEntity<
            ApiResponse<ConversationOrchestratorResponseDto>>
    processDtmf(
            @Valid
            @RequestBody
            ProcessDtmfRequestDto request) {

        ConversationOrchestratorResponseDto response =
                conversationOrchestratorService.processDtmf(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                ConversationOrchestratorMessages
                                        .DTMF_PROCESSED,
                                response
                        )
                );
    }

    /**
     * Processes a caller barge-in event.
     *
     * @param request barge-in request
     * @return conversation runtime response
     */
    @PostMapping("/barge-in")
    public ResponseEntity<
            ApiResponse<ConversationOrchestratorResponseDto>>
    processBargeIn(
            @Valid
            @RequestBody
            BargeInRequestDto request) {

        ConversationOrchestratorResponseDto response =
                conversationOrchestratorService.processBargeIn(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                ConversationOrchestratorMessages
                                        .BARGE_IN_PROCESSED,
                                response
                        )
                );
    }

    /**
     * Ends an active conversation.
     *
     * @param request conversation termination request
     * @return conversation runtime response
     */
    @PostMapping("/end")
    public ResponseEntity<
            ApiResponse<ConversationOrchestratorResponseDto>>
    end(
            @Valid
            @RequestBody
            EndConversationRequestDto request) {

        ConversationOrchestratorResponseDto response =
                conversationOrchestratorService.end(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                ConversationOrchestratorMessages
                                        .CONVERSATION_ENDED,
                                response
                        )
                );
    }
}