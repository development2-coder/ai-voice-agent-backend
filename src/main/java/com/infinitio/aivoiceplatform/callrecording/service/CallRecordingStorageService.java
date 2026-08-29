package com.infinitio.aivoiceplatform.callrecording.service;

import java.io.IOException;

/**
 * Storage service for complete call recordings.
 *
 * <p>
 * This service stores the complete audio recording of a
 * telephony call. It is separate from individual TTS audio.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallRecordingStorageService {

    /**
     * Downloads and stores a provider recording.
     *
     * @param recordingUrl provider recording URL
     * @param callPublicId platform call public ID
     * @return stored recording information
     * @throws IOException when storage fails
     */
    StoredCallRecording store(
            String recordingUrl,
            String callPublicId
    ) throws IOException;

    /**
     * Information about a locally stored recording.
     */
    record StoredCallRecording(
            String fileName,
            String filePath,
            String fileType,
            String contentType,
            Long sizeBytes
    ) {
    }
}