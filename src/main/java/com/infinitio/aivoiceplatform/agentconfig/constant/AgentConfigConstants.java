package com.infinitio.aivoiceplatform.agentconfig.constant;

/**
 * Constants used by Agent Configuration module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class AgentConfigConstants {

    private AgentConfigConstants() {
    }


    // =========================================================
    // FIELD LENGTHS
    // =========================================================

    public static final int PROVIDER_MAX_LENGTH = 50;

    public static final int MODEL_MAX_LENGTH = 100;

    public static final int LANGUAGE_MAX_LENGTH = 30;

    public static final int VOICE_MAX_LENGTH = 100;

    public static final int GREETING_MAX_LENGTH = 1000;

    public static final int PROMPT_MAX_LENGTH = 10000;

    public static final int STATUS_MAX_LENGTH = 30;


    // =========================================================
    // STATUS
    // =========================================================

    public static final String STATUS_DRAFT =
            "DRAFT";

    public static final String STATUS_ACTIVE =
            "ACTIVE";

    public static final String STATUS_INACTIVE =
            "INACTIVE";


    // =========================================================
    // DEFAULT VALUES
    // =========================================================

    public static final String DEFAULT_LANGUAGE =
            "en-IN";

    public static final double DEFAULT_TEMPERATURE =
            0.70;

    public static final int DEFAULT_MAX_TOKENS =
            1000;
}