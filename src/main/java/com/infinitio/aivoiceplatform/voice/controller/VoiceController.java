package com.infinitio.aivoiceplatform.voice.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.voice.constant.VoiceMessages;
import com.infinitio.aivoiceplatform.voice.dto.request.CreateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.request.UpdateVoiceRequest;
import com.infinitio.aivoiceplatform.voice.dto.response.VoiceResponse;
import com.infinitio.aivoiceplatform.voice.service.VoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Voice Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/voices")
@Tag(
        name = "Voice",
        description = "AI Voice Configuration APIs"
)
public class VoiceController {

    private final VoiceService voiceService;

    @Operation(summary = "Create Voice")
    @PostMapping
    public ResponseEntity<ApiResponse<VoiceResponse>> create(
            @Valid @RequestBody CreateVoiceRequest request) {

        log.info("REST Request : Create Voice");

        VoiceResponse response =
                voiceService.create(request);

        return ResponseBuilder.created(
                response,
                VoiceMessages.CREATED
        );
    }

    @Operation(summary = "Update Voice")
    @PutMapping
    public ResponseEntity<ApiResponse<VoiceResponse>> update(
            @Valid @RequestBody UpdateVoiceRequest request) {

        log.info("REST Request : Update Voice");

        VoiceResponse response =
                voiceService.update(request);

        return ResponseBuilder.success(
                response,
                VoiceMessages.UPDATED
        );
    }

    @Operation(summary = "Get Voice By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<VoiceResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Voice : {}",
                publicId
        );

        VoiceResponse response =
                voiceService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Voice fetched successfully."
        );
    }

    @Operation(summary = "Get All Voices")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VoiceResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All Voices. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<VoiceResponse> response =
                voiceService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Voices fetched successfully."
        );
    }

    @Operation(summary = "Delete Voice")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Voice : {}",
                publicId
        );

        voiceService.delete(publicId);

        return ResponseBuilder.success(
                null,
                VoiceMessages.DELETED
        );
    }

    @Operation(summary = "Activate Voice")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Voice : {}",
                publicId
        );

        voiceService.activate(publicId);

        return ResponseBuilder.success(
                null,
                VoiceMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Voice")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Voice : {}",
                publicId
        );

        voiceService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                VoiceMessages.DEACTIVATED
        );
    }
}