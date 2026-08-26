package com.infinitio.aivoiceplatform.llm.constant;

/**
 * Messages used by LLM module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class LlmMessages {

    private LlmMessages() {
    }

    /*
     * LLM configuration messages.
     */

    public static final String CREATED =
            "LLM configuration created successfully.";

    public static final String UPDATED =
            "LLM configuration updated successfully.";

    public static final String DELETED =
            "LLM configuration deleted successfully.";

    public static final String ACTIVATED =
            "LLM configuration activated successfully.";

    public static final String DEACTIVATED =
            "LLM configuration deactivated successfully.";

    public static final String NOT_FOUND =
            "LLM configuration not found.";

    public static final String CODE_ALREADY_EXISTS =
            "LLM code already exists.";

    public static final String NAME_ALREADY_EXISTS =
            "LLM name already exists.";

    /*
     * LLM runtime request validation messages.
     */

    public static final String GENERATION_REQUEST_REQUIRED =
            "LLM generation request is required.";

    public static final String CALL_ID_REQUIRED =
            "Call ID is required.";

    public static final String LANGUAGE_REQUIRED =
            "Language is required.";

    public static final String MESSAGES_REQUIRED =
            "LLM messages are required.";

    public static final String MESSAGE_ROLE_REQUIRED =
            "LLM message role is required.";

    public static final String MESSAGE_CONTENT_REQUIRED =
            "LLM message content is required.";

    public static final String LANGUAGE_NOT_SUPPORTED =
            "Requested language is not supported.";

    /*
     * LLM runtime provider messages.
     */

    public static final String PROVIDER_NOT_CONFIGURED =
            "LLM provider is not configured.";

    public static final String PROVIDER_UNAVAILABLE =
            "LLM provider is currently unavailable.";

    public static final String GENERATION_FAILED =
            "LLM generation failed.";

    public static final String SARVAM_GENERATION_FAILED =
            "Sarvam LLM generation failed.";

    public static final String EMPTY_PROVIDER_RESPONSE =
            "LLM provider returned an empty response.";

    public static final String INVALID_PROVIDER_RESPONSE =
            "LLM provider returned an invalid response.";
}