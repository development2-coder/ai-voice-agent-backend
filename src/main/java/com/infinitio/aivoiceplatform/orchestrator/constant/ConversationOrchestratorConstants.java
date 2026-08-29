package com.infinitio.aivoiceplatform.orchestrator.constant;

/**
 * Constants used by the Conversation Orchestrator module.
 *
 * <p>
 * These constants define runtime actions, context keys and
 * default values used while coordinating an active voice
 * conversation.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class ConversationOrchestratorConstants {

    private ConversationOrchestratorConstants() {
    }

    /**
     * Default language used when no language is supplied
     * by the runtime caller or STT provider.
     */
    public static final String DEFAULT_LANGUAGE =
            "en-IN";

    /**
     * Runtime action indicating that the Voice Gateway
     * should wait for caller input.
     */
    public static final String ACTION_LISTEN =
            "LISTEN";

    /**
     * Runtime action indicating that assistant audio
     * should be played.
     */
    public static final String ACTION_SPEAK =
            "SPEAK";

    /**
     * Runtime action indicating that the conversation
     * should wait for an external operation.
     */
    public static final String ACTION_WAIT =
            "WAIT";

    /**
     * Runtime action indicating that the call should
     * be transferred.
     */
    public static final String ACTION_TRANSFER =
            "TRANSFER";

    /**
     * Runtime action indicating that the conversation
     * should be terminated.
     */
    public static final String ACTION_END =
            "END";

    /**
     * Runtime action indicating that an API operation
     * is currently pending.
     */
    public static final String ACTION_WAIT_FOR_API =
            "WAIT_FOR_API";

    /**
     * Runtime action indicating that an AI operation
     * is currently pending.
     */
    public static final String ACTION_WAIT_FOR_AI =
            "WAIT_FOR_AI";

    /**
     * Runtime action indicating that a timer is pending.
     */
    public static final String ACTION_WAIT_FOR_TIMER =
            "WAIT_FOR_TIMER";

    /**
     * Flow context key containing the caller transcript.
     */
    public static final String CONTEXT_USER_INPUT =
            "userInput";

    /**
     * Flow context key containing the DTMF digit.
     */
    public static final String CONTEXT_DTMF =
            "dtmf";

    /**
     * Flow context key containing the detected language.
     */
    public static final String CONTEXT_LANGUAGE =
            "language";

    /**
     * Flow context key containing the latest assistant response.
     */
    public static final String CONTEXT_ASSISTANT_RESPONSE =
            "assistantResponse";

    /**
     * Flow context key containing the latest generated
     * assistant audio URL.
     */
    public static final String CONTEXT_AUDIO_URL =
            "audioUrl";

    /**
     * Flow context key indicating a caller barge-in event.
     */
    public static final String CONTEXT_BARGE_IN =
            "bargeIn";

    /**
     * Conversation role used for caller messages.
     */
    public static final String ROLE_USER =
            "user";

    /**
     * Conversation role used for assistant messages.
     */
    public static final String ROLE_ASSISTANT =
            "assistant";

    /**
     * Conversation role used for system instructions.
     */
    public static final String ROLE_SYSTEM =
            "system";

    /**
     * Maximum number of digits accepted as a single DTMF
     * runtime input.
     */
    public static final int MAX_DTMF_INPUT_LENGTH =
            32;
}