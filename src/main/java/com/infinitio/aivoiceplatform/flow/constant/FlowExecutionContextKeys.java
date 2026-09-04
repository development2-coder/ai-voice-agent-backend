package com.infinitio.aivoiceplatform.flow.constant;

/**
 * Internal keys used by the Flow execution runtime.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class FlowExecutionContextKeys {

    private FlowExecutionContextKeys() {
    }

    /**
     * Output port selected by the currently executing node.
     */
    public static final String SELECTED_OUTPUT_PORT =
            "_selectedOutputPort";

    /**
     * Variable used while waiting for user input.
     */
    public static final String WAITING_VARIABLE =
            "_waitingVariable";

    /**
     * Variable used while waiting for an API response.
     */
    public static final String WAITING_API_VARIABLE =
            "_waitingApiVariable";

    /**
     * Variable used while waiting for an AI response.
     */
    public static final String WAITING_AI_VARIABLE =
            "_waitingAiVariable";

    /**
     * API request temporary context.
     */
    public static final String API_REQUEST =
            "_apiRequest";

    /**
     * AI prompt temporary context.
     */
    public static final String AI_PROMPT =
            "_aiPrompt";

    /**
     * Timestamp at which a WAIT node may resume.
     */
    public static final String WAIT_RESUME_AT =
            "_waitResumeAt";

    /**
     * Duration configured for the WAIT node.
     */
    public static final String WAIT_DURATION_SECONDS =
            "_waitDurationSeconds";

    /**
     * Node public ID that initiated the WAIT.
     */
    public static final String WAIT_NODE_PUBLIC_ID =
            "_waitNodePublicId";

    /**
     * Tenant public identifier available to the Flow runtime.
     */
    public static final String TENANT_ID =
            "tenantId";

    /**
     * Agent public identifier available to the Flow runtime.
     */
    public static final String AGENT_ID =
            "agentId";

    /**
     * Flow public identifier available to the Flow runtime.
     */
    public static final String FLOW_PUBLIC_ID =
            "flowPublicId";

    /**
     * Call public identifier available to the Flow runtime.
     */
    public static final String CALL_ID =
            "callId";

    /**
     * Conversation public identifier available to the Flow runtime.
     */
    public static final String CONVERSATION_PUBLIC_ID =
            "conversationPublicId";

    /**
     * Conversation language available to the Flow runtime.
     */
    public static final String LANGUAGE =
            "language";

    /**
     * Resolved Agent runtime configuration.
     *
     * <p>
     * This value is populated by the Conversation Orchestrator
     * and must not be supplied by the external telephony provider.
     * </p>
     */
    public static final String RUNTIME_CONFIGURATION =
            "runtimeConfiguration";

    /**
     * Provider-specific call identifier.
     */
    public static final String PROVIDER_CALL_ID =
            "providerCallId";

    /**
     * Telephony provider code.
     */
    public static final String TELEPHONY_PROVIDER =
            "telephonyProvider";

    /**
     * Destination requested by a TRANSFER node.
     */
    public static final String TRANSFER_DESTINATION =
            "transferDestination";

    /**
     * Optional message associated with a TRANSFER node.
     */
    public static final String TRANSFER_MESSAGE =
            "transferMessage";
}