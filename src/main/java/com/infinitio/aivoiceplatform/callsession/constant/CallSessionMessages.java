package com.infinitio.aivoiceplatform.callsession.constant;

/**
 * Contains messages used by the call session module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class CallSessionMessages {

    private CallSessionMessages() {
    }

    public static final String CALL_SESSION_NOT_FOUND =
            "Call session not found.";

    public static final String CALL_SESSION_ALREADY_EXISTS =
            "Call session already exists.";

    public static final String CALL_SESSION_REQUEST_REQUIRED =
            "Call session request is required.";

    public static final String CALL_SESSION_UPDATE_REQUEST_REQUIRED =
            "Call session update request is required.";

    public static final String CONVERSATION_MESSAGE_REQUEST_REQUIRED =
            "Conversation message request is required.";

    public static final String CONVERSATION_NOT_ALLOWED_AFTER_END =
            "Conversation message cannot be added after the call session has ended.";

    public static final String CALL_ID_REQUIRED =
            "Call ID is required.";

    public static final String TENANT_ID_REQUIRED =
            "Tenant ID is required.";

    public static final String AGENT_ID_REQUIRED =
            "Agent ID is required.";

    public static final String AGENT_VERSION_REQUIRED =
            "Agent version is required.";

    public static final String AGENT_VERSION_INVALID =
            "Agent version must be greater than zero.";

    public static final String CONVERSATION_ROLE_REQUIRED =
            "Conversation message role is required.";

    public static final String CONVERSATION_TEXT_REQUIRED =
            "Conversation message text is required.";

    public static final String FLOW_NODE_ID_REQUIRED =
            "Flow node ID is required.";

    public static final String COLLECTED_SLOT_NAME_REQUIRED =
            "Collected slot name is required.";

    public static final String CALL_SESSION_STATUS_REQUIRED =
            "Call session status is required.";

    public static final String CALL_SESSION_CREATED =
            "Call session created successfully.";

    public static final String CALL_SESSION_FETCHED =
            "Call session fetched successfully.";

    public static final String CALL_SESSION_UPDATED =
            "Call session updated successfully.";

    public static final String CONVERSATION_MESSAGE_ADDED =
            "Conversation message added successfully.";

    public static final String FLOW_STATE_UPDATED =
            "Call session flow state updated successfully.";

    public static final String COLLECTED_SLOT_UPDATED =
            "Collected slot updated successfully.";

    public static final String CALL_SESSION_STATUS_UPDATED =
            "Call session status updated successfully.";
}