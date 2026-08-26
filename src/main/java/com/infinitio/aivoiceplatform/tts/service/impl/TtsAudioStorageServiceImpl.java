package com.infinitio.aivoiceplatform.tts.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.infinitio.aivoiceplatform.tts.config.TtsProperties;
import com.infinitio.aivoiceplatform.tts.constant.TtsMessages;
import com.infinitio.aivoiceplatform.tts.dto.runtime.TtsAudioStorageResponse;
import com.infinitio.aivoiceplatform.tts.service.TtsAudioStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements backend storage of generated TTS audio.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TtsAudioStorageServiceImpl
        implements TtsAudioStorageService {

    private final TtsProperties ttsProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public TtsAudioStorageResponse store(
            byte[] audioBytes,
            String contentType,
            String callId) {

        if (audioBytes == null
                || audioBytes.length == 0) {

            log.error(
                    "Generated TTS audio is empty. callId={}",
                    callId
            );

            throw new IllegalStateException(
                    TtsMessages.AUDIO_EMPTY
            );
        }

        String storagePath =
                ttsProperties.getAudioStoragePath();

        if (storagePath == null
                || storagePath.isBlank()) {

            log.error(
                    "TTS audio storage path is not configured. callId={}",
                    callId
            );

            throw new IllegalStateException(
                    TtsMessages.AUDIO_STORAGE_FAILED
            );
        }

        String safeCallId =
                sanitizeFileName(callId);

        String fileExtension =
                resolveFileExtension(contentType);

        String fileName =
                "tts_"
                        + safeCallId
                        + "_"
                        + UUID.randomUUID()
                        + fileExtension;

        Path directory =
                Paths.get(storagePath)
                        .toAbsolutePath()
                        .normalize();

        Path filePath =
                directory.resolve(fileName);

        try {

            Files.createDirectories(directory);

            Files.write(
                    filePath,
                    audioBytes,
                    StandardOpenOption.CREATE_NEW
            );

            log.info(
                    "TTS audio file stored successfully. callId={}, fileName={}, sizeBytes={}, path={}",
                    callId,
                    fileName,
                    audioBytes.length,
                    filePath
            );

            String audioBaseUrl =
                    ttsProperties.getAudioBaseUrl();

            if (audioBaseUrl == null
                    || audioBaseUrl.isBlank()) {

                log.warn(
                        "TTS audio base URL is not configured. callId={}, fileName={}",
                        callId,
                        fileName
                );

                return TtsAudioStorageResponse
                        .builder()
                        .fileName(fileName)
                        .audioUrl(null)
                        .contentType(contentType)
                        .sizeBytes(
                                (long) audioBytes.length
                        )
                        .build();
            }

            String audioUrl =
                    buildAudioUrl(
                            audioBaseUrl,
                            fileName
                    );

            return TtsAudioStorageResponse
                    .builder()
                    .fileName(fileName)
                    .audioUrl(audioUrl)
                    .contentType(contentType)
                    .sizeBytes(
                            (long) audioBytes.length
                    )
                    .build();

        } catch (IOException exception) {

            log.error(
                    "Failed to store TTS audio file. callId={}, fileName={}, path={}",
                    callId,
                    fileName,
                    filePath,
                    exception
            );

            throw new IllegalStateException(
                    TtsMessages.AUDIO_STORAGE_FAILED,
                    exception
            );
        }
    }

    /**
     * Resolves the generated audio file extension.
     *
     * @param contentType audio content type
     * @return file extension
     */
    private String resolveFileExtension(
            String contentType) {

        if ("audio/mpeg".equalsIgnoreCase(
                contentType)) {

            return ".mp3";
        }

        if ("audio/mp3".equalsIgnoreCase(
                contentType)) {

            return ".mp3";
        }

        if ("audio/wav".equalsIgnoreCase(
                contentType)) {

            return ".wav";
        }

        if ("audio/wave".equalsIgnoreCase(
                contentType)) {

            return ".wav";
        }

        return ".audio";
    }

    /**
     * Sanitizes the call identifier for use in a file name.
     *
     * @param callId call identifier
     * @return safe file name value
     */
    private String sanitizeFileName(
            String callId) {

        if (callId == null
                || callId.isBlank()) {

            return "unknown";
        }

        return callId
                .replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );
    }

    /**
     * Builds the public audio URL.
     *
     * @param baseUrl configured base URL
     * @param fileName generated file name
     * @return complete audio URL
     */
    private String buildAudioUrl(
            String baseUrl,
            String fileName) {

        String normalizedBaseUrl =
                baseUrl.endsWith("/")
                        ? baseUrl.substring(
                        0,
                        baseUrl.length() - 1
                )
                        : baseUrl;

        return normalizedBaseUrl
                + "/"
                + fileName;
    }
}