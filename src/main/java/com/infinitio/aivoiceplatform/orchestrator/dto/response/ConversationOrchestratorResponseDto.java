package com.infinitio.aivoiceplatform.orchestrator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Response DTO returned by the Conversation Orchestrator.
 *
 * <p>
 * Represents the current runtime result of a conversation
 * turn and provides the Voice Gateway with the information
 * required for the next runtime action.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationOrchestratorResponseDto {

    /**
     * Unique public identifier of the call.
     */
    private String callId;

    /**
     * Unique public identifier of the flow execution.
     */
    private String flowExecutionPublicId;

    /**
     * Current flow node public identifier.
     */
    private String currentNodePublicId;

    /**
     * Current flow node type.
     */
    private String currentNodeType;

    /**
     * Current flow execution status.
     */
    private String flowExecutionStatus;

    /**
     * Runtime action required by the Voice Gateway.
     *
     * <p>
     * Examples include SPEAK, LISTEN, WAIT, TRANSFER
     * and END.
     * </p>
     */
    private String action;

    /**
     * Caller transcript associated with the current turn.
     */
    private String transcript;

    /**
     * Assistant response text.
     */
    private String responseText;

    /**
     * Generated assistant audio encoded as Base64.
     *
     * <p>
     * This field is optional because audio may instead be
     * exposed through an audio URL.
     * </p>
     */
    private String audioBase64;

    /**
     * URL of the generated assistant audio.
     */
    private String audioUrl;

    /**
     * Generated audio file name.
     */
    private String audioFileName;

    /**
     * MIME type of the generated audio.
     */
    private String audioContentType;

    /**
     * Indicates that the flow is waiting for caller input.
     */
    private boolean waitingForUser;

    /**
     * Indicates that the flow is waiting for AI processing.
     */
    private boolean waitingForAi;

    /**
     * Indicates that the flow is waiting for an API response.
     */
    private boolean waitingForApi;

    /**
     * Indicates that the flow is waiting for a timer.
     */
    private boolean waitingForTimer;

    /**
     * Indicates that the call has been transferred.
     */
    private boolean transferred;

    /**
     * Indicates that the conversation has completed.
     */
    private boolean completed;

    /**
     * Runtime context returned by the flow.
     */
    private Map<String, Object> context;
}