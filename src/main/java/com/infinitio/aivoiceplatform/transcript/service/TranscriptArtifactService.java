package com.infinitio.aivoiceplatform.transcript.service;

import java.util.Map;

public interface TranscriptArtifactService {

    /**
     * Appends one conversation message to the compressed
     * JSON.GZ transcript.
     *
     * @param callPublicId call public identifier
     * @param message transcript message
     * @return filesystem path
     */
    String append(
            String callPublicId,
            Map<String, Object> message
    );
}