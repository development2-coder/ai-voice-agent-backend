package com.infinitio.aivoiceplatform.tts.dto.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents information about a stored TTS audio file.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsAudioStorageResponse {

    /**
     * Stored audio file name.
     */
    private String fileName;

    /**
     * Public URL of the stored audio file.
     */
    private String audioUrl;

    /**
     * Stored audio content type.
     */
    private String contentType;

    /**
     * Size of the stored audio in bytes.
     */
    private Long sizeBytes;
}