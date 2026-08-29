package com.infinitio.aivoiceplatform.callrecording.service.impl;

import com.infinitio.aivoiceplatform.callrecording.service.CallRecordingStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local filesystem storage for complete call recordings.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
public class LocalCallRecordingStorageServiceImpl
        implements CallRecordingStorageService {

    private final RestClient restClient;

    private final Path storageDirectory;

    public LocalCallRecordingStorageServiceImpl(
            @Value(
                    "${CALL_RECORDING_STORAGE_PATH:uploads/call-recordings}"
            )
            String storagePath) {

        this.restClient =
                RestClient.builder()
                        .build();

        this.storageDirectory =
                Paths.get(
                                storagePath
                        )
                        .toAbsolutePath()
                        .normalize();
    }

    /**
     * Downloads and stores the complete call recording.
     *
     * @param recordingUrl provider recording URL
     * @param callPublicId platform call public ID
     * @return stored recording information
     * @throws IOException when the recording cannot be stored
     */
    @Override
    public StoredCallRecording store(
            String recordingUrl,
            String callPublicId)
            throws IOException {

        if (recordingUrl == null
                || recordingUrl.isBlank()) {

            throw new IllegalArgumentException(
                    "Recording URL is required."
            );
        }

        if (callPublicId == null
                || callPublicId.isBlank()) {

            throw new IllegalArgumentException(
                    "Call public ID is required."
            );
        }

        Files.createDirectories(
                storageDirectory
        );

        String safeCallPublicId =
                sanitize(
                        callPublicId
                );

        String fileName =
                "call-recording-"
                        + safeCallPublicId
                        + ".mp3";

        Path targetPath =
                storageDirectory
                        .resolve(fileName)
                        .normalize();

        if (!targetPath.startsWith(
                storageDirectory
        )) {

            throw new IOException(
                    "Invalid call recording storage path."
            );
        }

        log.info(
                "Downloading complete call recording. "
                        + "callPublicId={}, recordingUrl={}",
                callPublicId,
                recordingUrl
        );

        byte[] audioBytes =
                restClient
                        .get()
                        .uri(
                                URI.create(
                                        recordingUrl
                                )
                        )
                        .header(
                                HttpHeaders.ACCEPT,
                                "audio/mpeg,audio/*"
                        )
                        .retrieve()
                        .body(
                                byte[].class
                        );

        if (audioBytes == null
                || audioBytes.length == 0) {

            throw new IOException(
                    "Provider returned an empty call recording."
            );
        }

        Files.write(
                targetPath,
                audioBytes
        );

        Long sizeBytes =
                Files.size(
                        targetPath
                );

        String relativePath =
                storageDirectory
                        .relativize(
                                targetPath
                        )
                        .toString()
                        .replace(
                                '\\',
                                '/'
                        );

        log.info(
                "Complete call recording stored successfully. "
                        + "callPublicId={}, fileName={}, "
                        + "filePath={}, sizeBytes={}",
                callPublicId,
                fileName,
                relativePath,
                sizeBytes
        );

        return new StoredCallRecording(
                fileName,
                relativePath,
                "mp3",
                "audio/mpeg",
                sizeBytes
        );
    }

    /**
     * Sanitizes a filename component.
     *
     * @param value input value
     * @return safe filename value
     */
    private String sanitize(
            String value) {

        return value.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );
    }
}