package com.infinitio.aivoiceplatform.stt.constant;

/**
 * Messages used by STT module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class SttMessages {

    private SttMessages() {
    }

    public static final String CREATED =
            "STT configuration created successfully.";

    public static final String UPDATED =
            "STT configuration updated successfully.";

    public static final String DELETED =
            "STT configuration deleted successfully.";

    public static final String ACTIVATED =
            "STT configuration activated successfully.";

    public static final String DEACTIVATED =
            "STT configuration deactivated successfully.";

    public static final String NOT_FOUND =
            "STT configuration not found.";

    public static final String CODE_ALREADY_EXISTS =
            "STT code already exists.";

    public static final String NAME_ALREADY_EXISTS =
            "STT name already exists.";

    public static final String TRANSCRIPTION_REQUEST_REQUIRED =
            "STT transcription request is required.";

    public static final String CALL_ID_REQUIRED =
            "Call ID is required.";

    public static final String AUDIO_REQUIRED =
            "Audio data is required.";

    public static final String AUDIO_READ_FAILED =
            "Unable to read uploaded audio file.";

    public static final String LANGUAGE_REQUIRED =
            "Language is required.";

    public static final String LANGUAGE_NOT_SUPPORTED =
            "Requested language is not supported.";

    public static final String AUDIO_SIZE_EXCEEDED =
            "Audio size exceeds the configured maximum limit.";

    public static final String PROVIDER_NOT_CONFIGURED =
            "STT provider is not configured.";

    public static final String PROVIDER_UNAVAILABLE =
            "STT provider is currently unavailable.";

    public static final String TRANSCRIPTION_FAILED =
            "Speech-to-text transcription failed.";

    public static final String SARVAM_TRANSCRIPTION_FAILED =
            "Sarvam speech-to-text transcription failed.";

    public static final String MODEL_NOT_CONFIGURED =
            "STT model is not configured.";

    /**
     * Streaming STT sample rate is required.
     */
    public static final String STREAMING_SAMPLE_RATE_REQUIRED =
            "STT streaming sample rate is required.";

    /**
     * Streaming STT sample rate must be positive.
     */
    public static final String STREAMING_SAMPLE_RATE_INVALID =
            "STT streaming sample rate must be greater than zero.";

    /**
     * Streaming STT listener is required.
     */
    public static final String STREAMING_LISTENER_REQUIRED =
            "STT streaming listener is required.";

    /**
     * Streaming STT audio encoding is required.
     */
    public static final String STREAMING_AUDIO_ENCODING_REQUIRED =
            "STT streaming audio encoding is required.";

    /**
     * Streaming STT session could not be opened.
     */
    public static final String STREAMING_SESSION_NOT_OPEN =
            "STT streaming session could not be opened.";

    /**
     * Streaming STT session is not active.
     */
    public static final String STREAMING_SESSION_NOT_ACTIVE =
            "No active streaming STT session for call.";

    /**
     * Streaming STT session is closed.
     */
    public static final String STREAMING_SESSION_CLOSED =
            "Streaming STT session is closed.";

    /**
     * Unable to start streaming STT.
     */
    public static final String STREAMING_START_FAILED =
            "Unable to start streaming STT.";

    /**
     * Unable to stream audio to STT provider.
     */
    public static final String STREAMING_AUDIO_FAILED =
            "Unable to stream audio to STT provider.";
}