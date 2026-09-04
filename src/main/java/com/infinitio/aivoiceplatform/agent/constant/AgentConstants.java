package com.infinitio.aivoiceplatform.agent.constant;

/**
 * Constants used by Agent module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class AgentConstants {

    private AgentConstants() {
    }

    public static final int AGENT_CODE_MAX_LENGTH = 50;

    public static final int AGENT_NAME_MAX_LENGTH = 150;

    public static final int DESCRIPTION_MAX_LENGTH = 500;

    public static final int WELCOME_MESSAGE_MAX_LENGTH = 1000;

    public static final int LANGUAGE_MAX_LENGTH = 50;

    public static final int STATUS_MAX_LENGTH = 30;

    public static final String INITIAL_FLOW_NAME =
            "Main Flow";

    public static final String INITIAL_FLOW_DESCRIPTION =
            "Default draft flow for the agent.";

    public static final String INITIAL_START_NODE_KEY =
            "start";

    public static final String INITIAL_START_NODE_NAME =
            "Start";

    public static final String INITIAL_END_NODE_KEY =
            "end";

    public static final String INITIAL_END_NODE_NAME =
            "End";

    public static final String MAIN_PORT =
            "main";

}