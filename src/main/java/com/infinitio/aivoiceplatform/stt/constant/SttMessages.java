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
}