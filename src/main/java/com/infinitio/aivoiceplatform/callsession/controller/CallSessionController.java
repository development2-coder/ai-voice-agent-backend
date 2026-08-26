package com.infinitio.aivoiceplatform.callsession.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infinitio.aivoiceplatform.callsession.dto.request.AddConversationMessageRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.CreateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCallSessionStatusRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateCollectedSlotRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.request.UpdateFlowStateRequestDto;
import com.infinitio.aivoiceplatform.callsession.dto.response.CallSessionResponseDto;
import com.infinitio.aivoiceplatform.callsession.service.CallSessionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides REST APIs for call-session management.
 *
 * <p>
 * Call-session runtime state is persisted in MySQL.
 * This controller does not directly access Redis or the repository.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/call-sessions")
public class CallSessionController {

    private final CallSessionService callSessionService;

    /**
     * Creates a new call session.
     *
     * @param request call-session creation request
     * @return created call session
     */
    @PostMapping
    public ResponseEntity<CallSessionResponseDto>
    createCallSession(
            @Valid
            @RequestBody
            CreateCallSessionRequestDto request) {

        log.info(
                "REST request to create call session. callId={}",
                request != null
                        ? request.getCallId()
                        : null
        );

        CallSessionResponseDto response =
                callSessionService.createCallSession(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Retrieves a call session by call identifier.
     *
     * @param callId public call identifier
     * @return call session
     */
    @GetMapping("/{callId}")
    public ResponseEntity<CallSessionResponseDto>
    getCallSession(
            @PathVariable
            String callId) {

        log.info(
                "REST request to get call session. callId={}",
                callId
        );

        return ResponseEntity.ok(
                callSessionService.getCallSession(
                        callId
                )
        );
    }

    /**
     * Updates general call-session information.
     *
     * @param callId public call identifier
     * @param request update request
     * @return updated call session
     */
    @PatchMapping("/{callId}")
    public ResponseEntity<CallSessionResponseDto>
    updateCallSession(
            @PathVariable
            String callId,
            @Valid
            @RequestBody
            UpdateCallSessionRequestDto request) {

        log.info(
                "REST request to update call session. callId={}",
                callId
        );

        return ResponseEntity.ok(
                callSessionService.updateCallSession(
                        callId,
                        request
                )
        );
    }

    /**
     * Updates the call-session status.
     *
     * @param callId public call identifier
     * @param request status update request
     * @return updated call session
     */
    @PatchMapping("/{callId}/status")
    public ResponseEntity<CallSessionResponseDto>
    updateStatus(
            @PathVariable
            String callId,
            @Valid
            @RequestBody
            UpdateCallSessionStatusRequestDto request) {

        log.info(
                "REST request to update call session status. callId={}, status={}",
                callId,
                request != null
                        ? request.getStatus()
                        : null
        );

        return ResponseEntity.ok(
                callSessionService.updateStatus(
                        callId,
                        request
                )
        );
    }

    /**
     * Updates the current flow execution state.
     *
     * @param callId public call identifier
     * @param request flow state update request
     * @return updated call session
     */
    @PatchMapping("/{callId}/flow")
    public ResponseEntity<CallSessionResponseDto>
    updateFlowState(
            @PathVariable
            String callId,
            @Valid
            @RequestBody
            UpdateFlowStateRequestDto request) {

        log.info(
                "REST request to update flow state. callId={}, flowNodeId={}",
                callId,
                request != null
                        ? request.getFlowNodeId()
                        : null
        );

        return ResponseEntity.ok(
                callSessionService.updateFlowState(
                        callId,
                        request
                )
        );
    }

    /**
     * Updates a collected slot.
     *
     * @param callId public call identifier
     * @param request collected slot request
     * @return updated call session
     */
    @PatchMapping("/{callId}/slots")
    public ResponseEntity<CallSessionResponseDto>
    updateCollectedSlot(
            @PathVariable
            String callId,
            @Valid
            @RequestBody
            UpdateCollectedSlotRequestDto request) {

        log.info(
                "REST request to update collected slot. callId={}, slotName={}",
                callId,
                request != null
                        ? request.getSlotName()
                        : null
        );

        return ResponseEntity.ok(
                callSessionService.updateCollectedSlot(
                        callId,
                        request
                )
        );
    }

    /**
     * Adds a conversation message to a call session.
     *
     * <p>
     * The conversation message is persisted as part of the
     * call-session state in MySQL.
     * </p>
     *
     * @param callId public call identifier
     * @param request conversation message request
     * @return updated call session
     */
    @PostMapping("/{callId}/conversation")
    public ResponseEntity<CallSessionResponseDto>
    addConversationMessage(
            @PathVariable
            String callId,
            @Valid
            @RequestBody
            AddConversationMessageRequestDto request) {

        log.info(
                "REST request to add conversation message. callId={}, role={}",
                callId,
                request != null
                        ? request.getRole()
                        : null
        );

        return ResponseEntity.ok(
                callSessionService.addConversationMessage(
                        callId,
                        request
                )
        );
    }

    /**
     * Soft deletes a call session.
     *
     * <p>
     * The call-session record remains in MySQL and is marked
     * as deleted instead of being physically removed.
     * </p>
     *
     * @param callId public call identifier
     * @return empty response
     */
    @DeleteMapping("/{callId}")
    public ResponseEntity<Void>
    deleteCallSession(
            @PathVariable
            String callId) {

        log.info(
                "REST request to delete call session. callId={}",
                callId
        );

        callSessionService.deleteCallSession(
                callId
        );

        log.info(
                "Call session deleted successfully. callId={}",
                callId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}