package com.infinitio.aivoiceplatform.orchestrator.constant;

/**
 * Message constants used by the Conversation Orchestrator module.
 *
 * <p>
 * Centralizes validation, runtime and lifecycle messages used
 * by the Conversation Orchestrator.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class ConversationOrchestratorMessages {

    private ConversationOrchestratorMessages() {
    }

    public static final String CONVERSATION_STARTED =
            "Conversation started successfully.";

    public static final String CONVERSATION_ENDED =
            "Conversation ended successfully.";

    public static final String CONVERSATION_AUDIO_PROCESSED =
            "Conversation audio processed successfully.";

    public static final String CONVERSATION_TRANSCRIPT_PROCESSED =
            "Conversation transcript processed successfully.";

    public static final String DTMF_PROCESSED =
            "DTMF input processed successfully.";

    public static final String BARGE_IN_PROCESSED =
            "Conversation barge-in processed successfully.";

    public static final String CALL_ID_REQUIRED =
            "Call ID is required.";

    public static final String TENANT_ID_REQUIRED =
            "Tenant ID is required.";

    public static final String AGENT_ID_REQUIRED =
            "Agent ID is required.";

    public static final String FLOW_PUBLIC_ID_REQUIRED =
            "Flow public ID is required.";

    public static final String AUDIO_REQUIRED =
            "Audio is required.";

    public static final String TRANSCRIPT_REQUIRED =
            "Transcript is required.";

    public static final String DTMF_DIGIT_REQUIRED =
            "DTMF digit is required.";

    public static final String CONVERSATION_NOT_FOUND =
            "Conversation session not found.";

    public static final String ACTIVE_FLOW_EXECUTION_NOT_FOUND =
            "Active flow execution not found.";

    public static final String FLOW_EXECUTION_REQUIRED =
            "Flow execution is required.";

    public static final String CONVERSATION_ALREADY_ENDED =
            "Conversation has already ended.";

    public static final String INVALID_AUDIO =
            "Invalid audio data.";

    public static final String INVALID_DTMF =
            "Invalid DTMF input.";

    public static final String STT_PROCESSING_FAILED =
            "Speech-to-text processing failed.";

    public static final String FLOW_PROCESSING_FAILED =
            "Flow processing failed.";

    public static final String LLM_PROCESSING_FAILED =
            "LLM processing failed.";

    public static final String TTS_PROCESSING_FAILED =
            "Text-to-speech processing failed.";

    public static final String CONVERSATION_PROCESSING_FAILED =
            "Conversation processing failed.";

    public static final String CONVERSATION_START_FAILED =
            "Conversation start failed.";

    public static final String CONVERSATION_END_FAILED =
            "Conversation end failed.";

    public static final String BARGE_IN_PROCESSING_FAILED =
            "Conversation barge-in processing failed.";

    public static final String DTMF_PROCESSING_FAILED =
            "DTMF processing failed.";

    public static final String FLOW_EXECUTION_FAILED =
            "Flow execution failed.";

    public static final String STT_RESPONSE_EMPTY =
            "Speech-to-text returned an empty response.";

    public static final String LLM_RESPONSE_EMPTY =
            "LLM returned an empty response.";

    public static final String TTS_RESPONSE_EMPTY =
            "Text-to-speech returned an empty response.";

    public static final String FLOW_EXECUTION_RESULT_EMPTY =
            "Flow execution returned an empty result.";

    public static final String CALL_SESSION_CREATION_FAILED =
            "Call session creation failed.";

    public static final String CALL_SESSION_UPDATE_FAILED =
            "Call session update failed.";
}