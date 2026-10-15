package com.infinitio.aivoiceplatform.tts.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.tts.constant.TtsMessages;
import com.infinitio.aivoiceplatform.tts.dto.request.CreateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.request.UpdateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.response.TtsResponse;
import com.infinitio.aivoiceplatform.tts.service.TtsService;
import com.infi.library.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for TTS Configuration.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tts")
@Tag(
        name = "TTS",
        description = "Text-to-Speech Configuration APIs"
)
public class TtsController {

    private final TtsService ttsService;

    /**
     * Creates a TTS configuration.
     *
     * @param request create request
     * @return created TTS
     */
    @Operation(summary = "Create TTS")
    @RequiresPermission("TTS_CREATE")
    @PostMapping
    public ResponseEntity<ApiResponse<TtsResponse>> create(
            @Valid @RequestBody CreateTtsRequest request) {

        log.info("REST Request : Create TTS");

        TtsResponse response =
                ttsService.create(request);

        return ResponseBuilder.created(
                response,
                TtsMessages.CREATED
        );
    }

    /**
     * Updates a TTS configuration.
     *
     * @param request update request
     * @return updated TTS
     */
    @Operation(summary = "Update TTS")
    @RequiresPermission("TTS_UPDATE")
    @PutMapping
    public ResponseEntity<ApiResponse<TtsResponse>> update(
            @Valid @RequestBody UpdateTtsRequest request) {

        log.info("REST Request : Update TTS");

        TtsResponse response =
                ttsService.update(request);

        return ResponseBuilder.success(
                response,
                TtsMessages.UPDATED
        );
    }

    /**
     * Retrieves a TTS configuration.
     *
     * @param publicId TTS public ID
     * @return TTS configuration
     */
    @Operation(summary = "Get TTS By Public Id")
    @RequiresPermission("TTS_VIEW")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<TtsResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get TTS : {}",
                publicId
        );

        TtsResponse response =
                ttsService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "TTS configuration fetched successfully."
        );
    }

    /**
     * Retrieves all TTS configurations.
     *
     * @param page page number
     * @param size page size
     * @return paginated TTS configurations
     */
    @Operation(summary = "Get All TTS Configurations")
    @RequiresPermission("TTS_VIEW")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TtsResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All TTS Configurations. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<TtsResponse> response =
                ttsService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "TTS configurations fetched successfully."
        );
    }

    /**
     * Soft deletes a TTS configuration.
     *
     * @param publicId TTS public ID
     * @return success response
     */
    @Operation(summary = "Delete TTS")
    @RequiresPermission("TTS_DELETE")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete TTS : {}",
                publicId
        );

        ttsService.delete(publicId);

        return ResponseBuilder.success(
                null,
                TtsMessages.DELETED
        );
    }

    /**
     * Activates a TTS configuration.
     *
     * @param publicId TTS public ID
     * @return success response
     */
    @Operation(summary = "Activate TTS")
    @RequiresPermission("TTS_UPDATE")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate TTS : {}",
                publicId
        );

        ttsService.activate(publicId);

        return ResponseBuilder.success(
                null,
                TtsMessages.ACTIVATED
        );
    }

    /**
     * Deactivates a TTS configuration.
     *
     * @param publicId TTS public ID
     * @return success response
     */
    @Operation(summary = "Deactivate TTS")
    @RequiresPermission("TTS_UPDATE")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate TTS : {}",
                publicId
        );

        ttsService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                TtsMessages.DEACTIVATED
        );
    }
}