package com.infinitio.aivoiceplatform.tts.constant;

/**
 * Messages used by TTS module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class TtsMessages {

    private TtsMessages() {
    }

    public static final String CREATED =
            "TTS configuration created successfully.";

    public static final String UPDATED =
            "TTS configuration updated successfully.";

    public static final String DELETED =
            "TTS configuration deleted successfully.";

    public static final String ACTIVATED =
            "TTS configuration activated successfully.";

    public static final String DEACTIVATED =
            "TTS configuration deactivated successfully.";

    public static final String NOT_FOUND =
            "TTS configuration not found.";

    public static final String CODE_ALREADY_EXISTS =
            "TTS code already exists.";

    public static final String NAME_ALREADY_EXISTS =
            "TTS name already exists.";

    /*
     * Runtime TTS messages.
     */

    public static final String SYNTHESIS_REQUEST_REQUIRED =
            "TTS synthesis request is required.";

    public static final String CALL_ID_REQUIRED =
            "Call ID is required.";

    public static final String TEXT_REQUIRED =
            "Text is required.";

    public static final String LANGUAGE_REQUIRED =
            "Language is required.";

    public static final String SPEAKER_REQUIRED =
            "Speaker is required.";

    public static final String LANGUAGE_NOT_SUPPORTED =
            "Requested language is not supported.";

    public static final String SPEAKER_NOT_SUPPORTED =
            "Requested speaker is not supported.";

    public static final String TEXT_SIZE_EXCEEDED =
            "Text exceeds the configured maximum limit.";

    public static final String PROVIDER_NOT_CONFIGURED =
            "TTS provider is not configured.";

    public static final String PROVIDER_UNAVAILABLE =
            "TTS provider is currently unavailable.";

    public static final String MODEL_NOT_CONFIGURED =
            "TTS model is not configured.";

    public static final String STREAM_PATH_NOT_CONFIGURED =
            "TTS streaming path is not configured.";

    public static final String API_KEY_NOT_CONFIGURED =
            "TTS API key is not configured.";

    public static final String API_KEY_HEADER_NOT_CONFIGURED =
            "TTS API key header is not configured.";

    public static final String DEFAULT_SPEAKER_NOT_CONFIGURED =
            "TTS default speaker is not configured.";

    public static final String OUTPUT_AUDIO_CODEC_NOT_CONFIGURED =
            "TTS output audio codec is not configured.";

    public static final String SYNTHESIS_FAILED =
            "Text-to-speech synthesis failed.";

    public static final String SARVAM_SYNTHESIS_FAILED =
            "Sarvam text-to-speech synthesis failed.";

    public static final String AUDIO_STORAGE_FAILED =
            "Generated TTS audio could not be stored.";

    public static final String AUDIO_EMPTY =
            "Generated TTS audio is empty.";
}