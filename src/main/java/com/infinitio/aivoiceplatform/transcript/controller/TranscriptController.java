package com.infinitio.aivoiceplatform.transcript.controller;

import com.infi.library.annotation.RequiresPermission;
import com.infinitio.aivoiceplatform.transcript.dto.request.CreateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.request.UpdateTranscriptRequest;
import com.infinitio.aivoiceplatform.transcript.dto.response.TranscriptResponse;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST APIs for transcript management.
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

    @PostMapping
    @RequiresPermission("TRANSCRIPT_CREATE")
    public ResponseEntity<TranscriptResponse> create(
            @Valid @RequestBody CreateTranscriptRequest request) {

        log.info(
                "Creating transcript. callPublicId={}, sequenceNumber={}",
                request.getCallPublicId(),
                request.getSequenceNumber()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transcriptService.create(request));
    }

    @GetMapping("/{publicId}")
    @RequiresPermission("TRANSCRIPT_VIEW")
    public ResponseEntity<TranscriptResponse> getByPublicId(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                transcriptService.getByPublicId(publicId)
        );
    }

    @GetMapping("/call/{callPublicId}")
    @RequiresPermission("TRANSCRIPT_VIEW")
    public ResponseEntity<List<TranscriptResponse>> getByCallPublicId(
            @PathVariable String callPublicId) {

        return ResponseEntity.ok(
                transcriptService.getByCallPublicId(callPublicId)
        );
    }

    @PutMapping("/{publicId}")
    @RequiresPermission("TRANSCRIPT_UPDATE")
    public ResponseEntity<TranscriptResponse> update(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateTranscriptRequest request) {

        return ResponseEntity.ok(
                transcriptService.update(publicId, request)
        );
    }

    @DeleteMapping("/{publicId}")
    @RequiresPermission("TRANSCRIPT_DELETE")
    public ResponseEntity<Void> delete(
            @PathVariable String publicId) {

        transcriptService.delete(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{publicId}/activate")
    @RequiresPermission("TRANSCRIPT_UPDATE")
    public ResponseEntity<Void> activate(
            @PathVariable String publicId) {

        transcriptService.activate(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{publicId}/deactivate")
    @RequiresPermission("TRANSCRIPT_UPDATE")
    public ResponseEntity<Void> deactivate(
            @PathVariable String publicId) {

        transcriptService.deactivate(publicId);
        return ResponseEntity.noContent().build();
    }
}