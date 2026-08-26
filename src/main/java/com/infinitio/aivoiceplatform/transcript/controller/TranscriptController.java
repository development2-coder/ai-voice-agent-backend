package com.infinitio.aivoiceplatform.transcript.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infinitio.aivoiceplatform.transcript.dto.request.CreateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.request.UpdateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.response.TranscriptResponse;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides REST APIs for transcript management.
 *
 * <p>
 * Transcript records are persisted in MySQL and are associated
 * directly with a call.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/transcripts")
public class TranscriptController {

    private final TranscriptService transcriptService;

    /**
     * Creates a transcript segment.
     *
     * @param request transcript creation request
     * @return created transcript
     */
    @PostMapping
    public ResponseEntity<TranscriptResponse> create(
            @Valid
            @RequestBody
            CreateTranscriptRequest request) {

        log.info(
                "REST request to create transcript. " +
                        "callPublicId={}, sequenceNumber={}",
                request.getCallPublicId(),
                request.getSequenceNumber()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transcriptService.create(
                                request
                        )
                );
    }

    /**
     * Retrieves a transcript by public identifier.
     *
     * @param publicId transcript public identifier
     * @return transcript
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<TranscriptResponse> getByPublicId(
            @PathVariable
            String publicId) {

        log.info(
                "REST request to get transcript. publicId={}",
                publicId
        );

        return ResponseEntity.ok(
                transcriptService.getByPublicId(
                        publicId
                )
        );
    }

    /**
     * Retrieves all transcripts belonging to a call.
     *
     * @param callPublicId call public identifier
     * @return call transcripts ordered by sequence number
     */
    @GetMapping("/call/{callPublicId}")
    public ResponseEntity<List<TranscriptResponse>>
    getByCallPublicId(
            @PathVariable
            String callPublicId) {

        log.info(
                "REST request to get transcripts by call. " +
                        "callPublicId={}",
                callPublicId
        );

        return ResponseEntity.ok(
                transcriptService
                        .getByCallPublicId(
                                callPublicId
                        )
        );
    }

    /**
     * Updates a transcript.
     *
     * @param publicId transcript public identifier
     * @param request update request
     * @return updated transcript
     */
    @PutMapping("/{publicId}")
    public ResponseEntity<TranscriptResponse> update(
            @PathVariable
            String publicId,
            @Valid
            @RequestBody
            UpdateTranscriptRequest request) {

        log.info(
                "REST request to update transcript. publicId={}",
                publicId
        );

        return ResponseEntity.ok(
                transcriptService.update(
                        publicId,
                        request
                )
        );
    }

    /**
     * Soft deletes a transcript.
     *
     * @param publicId transcript public identifier
     * @return empty response
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(
            @PathVariable
            String publicId) {

        log.info(
                "REST request to delete transcript. publicId={}",
                publicId
        );

        transcriptService.delete(
                publicId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    /**
     * Activates a transcript.
     *
     * @param publicId transcript public identifier
     * @return empty response
     */
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable
            String publicId) {

        log.info(
                "REST request to activate transcript. publicId={}",
                publicId
        );

        transcriptService.activate(
                publicId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    /**
     * Deactivates a transcript.
     *
     * @param publicId transcript public identifier
     * @return empty response
     */
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable
            String publicId) {

        log.info(
                "REST request to deactivate transcript. publicId={}",
                publicId
        );

        transcriptService.deactivate(
                publicId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}