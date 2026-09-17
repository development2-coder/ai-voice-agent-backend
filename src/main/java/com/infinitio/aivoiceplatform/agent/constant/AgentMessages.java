package com.infinitio.aivoiceplatform.agent.constant;

/**
 * Messages used by Agent module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class AgentMessages {

    private AgentMessages() {
    }

    public static final String AGENT_CREATED =
            "Agent created successfully.";

    public static final String AGENT_UPDATED =
            "Agent updated successfully.";

    public static final String AGENT_DELETED =
            "Agent deleted successfully.";

    public static final String AGENT_ACTIVATED =
            "Agent activated successfully.";

    public static final String AGENT_DEACTIVATED =
            "Agent deactivated successfully.";

    public static final String AGENT_NOT_FOUND =
            "Agent not found.";

    public static final String AGENT_COPIED =
            "Agent copied successfully.";

    public static final String CODE_ALREADY_EXISTS =
            "Agent code already exists.";

    public static final String NAME_ALREADY_EXISTS =
            "Agent name already exists.";

    /**
     * Same message as not-found so that cross-tenant Agent
     * existence is not exposed to unauthorized users.
     */
    public static final String AGENT_COPY_SOURCE_NOT_FOUND =
            "Agent not found.";

}