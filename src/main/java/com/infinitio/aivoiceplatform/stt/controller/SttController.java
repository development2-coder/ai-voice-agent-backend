package com.infinitio.aivoiceplatform.stt.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.stt.constant.SttMessages;
import com.infinitio.aivoiceplatform.stt.dto.request.CreateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.request.UpdateSttRequest;
import com.infinitio.aivoiceplatform.stt.dto.response.SttResponse;
import com.infinitio.aivoiceplatform.stt.service.SttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for STT Configuration.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stts")
@Tag(
        name = "STT",
        description = "Speech-to-Text Configuration APIs"
)
public class SttController {

    private final SttService sttService;

    @Operation(summary = "Create STT")
    @PostMapping
    public ResponseEntity<ApiResponse<SttResponse>> create(
            @Valid @RequestBody CreateSttRequest request) {

        log.info("REST Request : Create STT");

        SttResponse response =
                sttService.create(request);

        return ResponseBuilder.created(
                response,
                SttMessages.CREATED
        );
    }

    @Operation(summary = "Update STT")
    @PutMapping
    public ResponseEntity<ApiResponse<SttResponse>> update(
            @Valid @RequestBody UpdateSttRequest request) {

        log.info("REST Request : Update STT");

        SttResponse response =
                sttService.update(request);

        return ResponseBuilder.success(
                response,
                SttMessages.UPDATED
        );
    }

    @Operation(summary = "Get STT By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<SttResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get STT : {}",
                publicId
        );

        SttResponse response =
                sttService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "STT configuration fetched successfully."
        );
    }

    @Operation(summary = "Get All STT Configurations")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SttResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All STT Configurations. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<SttResponse> response =
                sttService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "STT configurations fetched successfully."
        );
    }

    @Operation(summary = "Delete STT")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete STT : {}",
                publicId
        );

        sttService.delete(publicId);

        return ResponseBuilder.success(
                null,
                SttMessages.DELETED
        );
    }

    @Operation(summary = "Activate STT")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate STT : {}",
                publicId
        );

        sttService.activate(publicId);

        return ResponseBuilder.success(
                null,
                SttMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate STT")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate STT : {}",
                publicId
        );

        sttService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                SttMessages.DEACTIVATED
        );
    }
}