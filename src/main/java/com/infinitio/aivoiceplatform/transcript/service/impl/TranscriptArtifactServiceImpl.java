package com.infinitio.aivoiceplatform.transcript.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.transcript.entity.TranscriptArtifact;
import com.infinitio.aivoiceplatform.transcript.repository.TranscriptArtifactRepository;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptArtifactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptArtifactServiceImpl
        implements TranscriptArtifactService {

    private static final Long SYSTEM_USER_ID = 1L;

    private static final String CONTENT_TYPE =
            "application/gzip";

    private final TranscriptArtifactRepository repository;

    private final ObjectMapper objectMapper;

    private final CurrentUserService currentUserService;

    /**
     * Existing conversation storage path.
     *
     * This uses the same storage location already configured
     * through CONVERSATION_STORAGE_PATH.
     */
    @Value("${voice.call-session.conversation-storage-path}")
    private String storagePath;

    /**
     * Configure the ObjectMapper used by this service.
     *
     * Jackson must have JavaTimeModule registered in order
     * to serialize LocalDateTime values such as:
     *
     * messages[].timestamp
     * updatedAt
     */
    private ObjectMapper getTranscriptObjectMapper() {

        ObjectMapper transcriptMapper =
                objectMapper.copy();

        /*
         * Register Java 8 date/time support explicitly.
         *
         * This protects the transcript service even if the
         * injected ObjectMapper is created/configured differently.
         */
        transcriptMapper.registerModule(
                new JavaTimeModule()
        );

        /*
         * Write LocalDateTime as ISO-8601 text instead of
         * timestamp arrays.
         *
         * Example:
         *
         * 2026-08-27T10:28:06.841380
         */
        transcriptMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        return transcriptMapper;
    }

    @Override
    @Transactional
    public synchronized String append(
            String callPublicId,
            Map<String, Object> message) {

        validate(
                callPublicId,
                message
        );

        if (storagePath == null
                || storagePath.isBlank()) {

            throw new IllegalStateException(
                    "Conversation storage path is not configured."
            );
        }

        Path directory =
                Paths.get(
                                storagePath
                        )
                        .toAbsolutePath()
                        .normalize();

        Path filePath =
                directory.resolve(
                        sanitize(callPublicId)
                                + ".json.gz"
                );

        /*
         * Use a mapper configured specifically for transcript
         * serialization.
         */
        ObjectMapper transcriptMapper =
                getTranscriptObjectMapper();

        /*
         * Read existing transcript if available.
         */
        Map<String, Object> document =
                readExisting(
                        filePath,
                        transcriptMapper
                );

        /*
         * Always maintain the call public ID.
         */
        document.put(
                "callPublicId",
                callPublicId
        );

        /*
         * Identify the artifact format.
         */
        document.put(
                "format",
                "json.gz"
        );

        /*
         * LocalDateTime is now safely handled by JavaTimeModule.
         */
        document.put(
                "updatedAt",
                LocalDateTime.now()
        );

        /*
         * Get the existing messages list.
         */
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages =
                (List<Map<String, Object>>)
                        document.computeIfAbsent(
                                "messages",
                                ignored ->
                                        new ArrayList<
                                                Map<String, Object>
                                                >()
                        );

        /*
         * Copy the incoming message so that we do not modify
         * the original map supplied by the caller.
         */
        Map<String, Object> messageCopy =
                new LinkedHashMap<>(
                        message
                );

        /*
         * Automatically assign sequence number if the caller
         * has not already supplied one.
         */
        messageCopy.putIfAbsent(
                "sequenceNumber",
                messages.size() + 1
        );

        /*
         * Add message to transcript.
         */
        messages.add(
                messageCopy
        );

        try {

            /*
             * Make sure the conversation storage directory exists.
             */
            Files.createDirectories(
                    directory
            );

            /*
             * Write to a temporary file first.
             *
             * This prevents a partially written gzip file from
             * becoming the active transcript artifact.
             */
            Path temporaryFile =
                    directory.resolve(
                            filePath.getFileName()
                                    .toString()
                                    + ".tmp"
                    );

            /*
             * Remove stale temporary file if it exists.
             */
            Files.deleteIfExists(
                    temporaryFile
            );

            /*
             * Write JSON -> GZIP -> temporary file.
             */
            try (
                    OutputStream outputStream =
                            Files.newOutputStream(
                                    temporaryFile
                            );

                    GZIPOutputStream gzipOutputStream =
                            new GZIPOutputStream(
                                    outputStream
                            )
            ) {

                transcriptMapper.writeValue(
                        gzipOutputStream,
                        document
                );
            }

            /*
             * Replace the existing transcript atomically when
             * supported by the operating system.
             *
             * If ATOMIC_MOVE is not supported, fall back to the
             * normal replace operation.
             */
            try {

                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {

                log.debug(
                        "Atomic move not supported. " +
                                "Using regular replace move. path={}",
                        filePath
                );

                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            /*
             * Get final artifact size.
             */
            long sizeBytes =
                    Files.size(
                            filePath
                    );

            /*
             * Resolve authenticated user.
             *
             * If no authenticated user is available, use the
             * system user.
             */
            Long userId =
                    resolveCreatedBy();

            /*
             * Find existing artifact for this call or create
             * a new one.
             */
            TranscriptArtifact artifact =
                    repository
                            .findTopByCallPublicIdOrderByCreatedAtDesc(
                                    callPublicId
                            )
                            .orElseGet(
                                    () ->
                                            TranscriptArtifact
                                                    .builder()
                                                    .callPublicId(
                                                            callPublicId
                                                    )
                                                    .createdBy(
                                                            userId
                                                    )
                                                    .build()
                            );

            /*
             * Update artifact metadata.
             */
            artifact.setFilePath(
                    filePath.toString()
            );

            artifact.setFileName(
                    filePath.getFileName()
                            .toString()
            );

            artifact.setContentType(
                    CONTENT_TYPE
            );

            artifact.setSizeBytes(
                    sizeBytes
            );

            artifact.setUpdatedBy(
                    userId
            );

            /*
             * Persist artifact metadata.
             */
            repository.save(
                    artifact
            );

            log.info(
                    "Transcript JSON.GZ stored successfully. " +
                            "callPublicId={}, path={}, sizeBytes={}, " +
                            "messageCount={}",
                    callPublicId,
                    filePath,
                    sizeBytes,
                    messages.size()
            );

            return filePath.toString();

        } catch (Exception exception) {

            /*
             * Clean up temporary file if anything failed.
             */
            try {

                Path temporaryFile =
                        directory.resolve(
                                filePath.getFileName()
                                        .toString()
                                        + ".tmp"
                        );

                Files.deleteIfExists(
                        temporaryFile
                );

            } catch (Exception cleanupException) {

                log.warn(
                        "Unable to clean temporary transcript file. " +
                                "callPublicId={}, path={}",
                        callPublicId,
                        filePath,
                        cleanupException
                );
            }

            log.error(
                    "Failed to store transcript JSON.GZ. " +
                            "callPublicId={}, path={}",
                    callPublicId,
                    filePath,
                    exception
            );

            throw new IllegalStateException(
                    "Failed to store transcript artifact.",
                    exception
            );
        }
    }

    /**
     * Reads an existing .json.gz transcript.
     */
    private Map<String, Object> readExisting(
            Path filePath,
            ObjectMapper transcriptMapper) {

        if (!Files.exists(filePath)) {

            return emptyDocument();
        }

        try (
                InputStream inputStream =
                        Files.newInputStream(
                                filePath
                        );

                GZIPInputStream gzipInputStream =
                        new GZIPInputStream(
                                inputStream
                        )
        ) {

            Map<String, Object> document =
                    transcriptMapper.readValue(
                            gzipInputStream,
                            new TypeReference<
                                    Map<String, Object>
                                    >() {
                            }
                    );

            if (document == null) {

                return emptyDocument();
            }

            /*
             * Make sure messages always exists and is a List.
             */
            if (!(document.get(
                    "messages"
            ) instanceof List)) {

                document.put(
                        "messages",
                        new ArrayList<
                                Map<String, Object>
                                >()
                );
            }

            return document;

        } catch (Exception exception) {

            /*
             * If the existing file is corrupted or cannot be
             * read, start a new transcript instead of failing
             * the complete call flow.
             */
            log.warn(
                    "Unable to read existing transcript. " +
                            "A new artifact will be created. path={}",
                    filePath,
                    exception
            );

            return emptyDocument();
        }
    }

    /**
     * Creates a new empty transcript document.
     */
    private Map<String, Object> emptyDocument() {

        Map<String, Object> document =
                new LinkedHashMap<>();

        document.put(
                "messages",
                new ArrayList<
                        Map<String, Object>
                        >()
        );

        return document;
    }

    /**
     * Resolves the user who created/updated the transcript.
     *
     * Falls back to SYSTEM_USER_ID when there is no authenticated
     * user, which is important for call execution initiated by
     * the runtime/telephony flow.
     */
    private Long resolveCreatedBy() {

        try {

            if (currentUserService.isAuthenticated()) {

                Long currentUserId =
                        currentUserService
                                .getCurrentUserId();

                if (currentUserId != null) {

                    return currentUserId;
                }
            }

        } catch (Exception exception) {

            log.debug(
                    "Unable to resolve authenticated user " +
                            "for transcript artifact.",
                    exception
            );
        }

        return SYSTEM_USER_ID;
    }

    /**
     * Validates transcript input.
     */
    private void validate(
            String callPublicId,
            Map<String, Object> message) {

        if (callPublicId == null
                || callPublicId.isBlank()) {

            throw new IllegalArgumentException(
                    "Call public ID is required for transcript storage."
            );
        }

        if (message == null
                || message.isEmpty()) {

            throw new IllegalArgumentException(
                    "Transcript message is required."
            );
        }
    }

    /**
     * Sanitizes callPublicId before using it as a file name.
     */
    private String sanitize(
            String value) {

        return value.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );
    }
}