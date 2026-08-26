package com.infinitio.aivoiceplatform.transcript.constant;

/**
 * Constants used by Transcript module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public final class TranscriptConstants {

    private TranscriptConstants() {
    }

    /**
     * Maximum length of speaker type.
     */
    public static final int SPEAKER_TYPE_MAX_LENGTH = 30;

    /**
     * Maximum length of language.
     */
    public static final int LANGUAGE_MAX_LENGTH = 20;

    /**
     * Maximum length of transcript text.
     */
    public static final int TEXT_MAX_LENGTH = 5000;

    /**
     * Maximum length of transcript source.
     */
    public static final int SOURCE_MAX_LENGTH = 30;

    /**
     * Minimum valid sequence number.
     */
    public static final int MIN_SEQUENCE_NUMBER = 1;

    /**
     * Default page number.
     */
    public static final int DEFAULT_PAGE = 0;

    /**
     * Default page size.
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Maximum page size.
     */
    public static final int MAX_PAGE_SIZE = 100;
}