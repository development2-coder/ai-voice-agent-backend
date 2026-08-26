package com.infinitio.aivoiceplatform.transcript.constant;

/**
 * Messages used by Transcript module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class TranscriptMessages {

    private TranscriptMessages() {
    }

    public static final String CREATED =
            "Transcript created successfully.";

    public static final String UPDATED =
            "Transcript updated successfully.";

    public static final String DELETED =
            "Transcript deleted successfully.";

    public static final String ACTIVATED =
            "Transcript activated successfully.";

    public static final String DEACTIVATED =
            "Transcript deactivated successfully.";

    public static final String NOT_FOUND =
            "Transcript not found.";

    public static final String SEQUENCE_NUMBER_ALREADY_EXISTS =
            "Transcript sequence number already exists.";

    public static final String CALL_NOT_FOUND =
            "Call not found.";

    public static final String CALL_RECORDING_NOT_FOUND =
            "Call recording not found.";

    public static final String CALL_ID_REQUIRED =
            "Call ID is required.";

    public static final String CALL_RECORDING_PUBLIC_ID_REQUIRED =
            "Call recording public ID is required.";

    public static final String TRANSCRIPT_PUBLIC_ID_REQUIRED =
            "Transcript public ID is required.";

    public static final String SEQUENCE_NUMBER_REQUIRED =
            "Sequence number is required.";

    public static final String SEQUENCE_NUMBER_INVALID =
            "Sequence number must be greater than zero.";

    public static final String SPEAKER_TYPE_REQUIRED =
            "Speaker type is required.";

    public static final String TEXT_REQUIRED =
            "Transcript text is required.";

    public static final String LANGUAGE_INVALID =
            "Transcript language is invalid.";

    public static final String SOURCE_INVALID =
            "Transcript source is invalid.";

    public static final String STARTED_AT_INVALID =
            "Transcript start time is invalid.";

    public static final String ENDED_AT_INVALID =
            "Transcript end time is invalid.";
}