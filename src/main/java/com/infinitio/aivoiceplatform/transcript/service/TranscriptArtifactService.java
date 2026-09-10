package com.infinitio.aivoiceplatform.transcript.service;

import java.util.*;

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

    /**
     * Reads all transcript messages stored for a call.
     *
     * @param callPublicId call public identifier
     * @return complete transcript messages
     */
    List<Map<String, Object>> readMessages(
            String callPublicId
    );
}