package com.infinitio.aivoiceplatform.callrecording.controller;

import com.infi.library.annotation.RequiresPermission;
import com.infinitio.aivoiceplatform.callrecording.constant.CallRecordingMessages;
import com.infinitio.aivoiceplatform.callrecording.dto.request.CreateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.request.UpdateCallRecordingRequest;
import com.infinitio.aivoiceplatform.callrecording.dto.response.CallRecordingResponse;
import com.infinitio.aivoiceplatform.callrecording.service.CallRecordingService;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for call recording management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/call-recordings")
@Tag(
        name = "Call Recording",
        description = "AI Voice Call Recording Management APIs"
)
public class CallRecordingController {

    private final CallRecordingService callRecordingService;

    @Operation(summary = "Create Call Recording")
    @PostMapping
    @RequiresPermission("CALL_RECORDING_CREATE")
    public ResponseEntity<ApiResponse<CallRecordingResponse>> create(
            @Valid @RequestBody CreateCallRecordingRequest request) {

        CallRecordingResponse response =
                callRecordingService.create(request);

        return ResponseBuilder.created(
                response,
                CallRecordingMessages.CREATED
        );
    }

    @Operation(summary = "Update Call Recording")
    @PutMapping
    @RequiresPermission("CALL_RECORDING_UPDATE")
    public ResponseEntity<ApiResponse<CallRecordingResponse>> update(
            @Valid @RequestBody UpdateCallRecordingRequest request) {

        CallRecordingResponse response =
                callRecordingService.update(request);

        return ResponseBuilder.success(
                response,
                CallRecordingMessages.UPDATED
        );
    }

    @Operation(summary = "Get Call Recording By Public ID")
    @GetMapping("/{publicId}")
    @RequiresPermission("CALL_RECORDING_VIEW")
    public ResponseEntity<ApiResponse<CallRecordingResponse>> getByPublicId(
            @PathVariable String publicId) {

        return ResponseBuilder.success(
                callRecordingService.getByPublicId(publicId),
                "Call recording fetched successfully."
        );
    }

    @Operation(summary = "Get All Accessible Call Recordings")
    @GetMapping
    @RequiresPermission("CALL_RECORDING_VIEW")
    public ResponseEntity<ApiResponse<PageResponse<CallRecordingResponse>>> getAll() {

        return ResponseBuilder.success(
                callRecordingService.getAll(),
                "Call recordings fetched successfully."
        );
    }

    @Operation(summary = "Get Recordings By Call")
    @GetMapping("/call/{callPublicId}")
    @RequiresPermission("CALL_RECORDING_VIEW")
    public ResponseEntity<ApiResponse<PageResponse<CallRecordingResponse>>> getByCall(
            @PathVariable String callPublicId) {

        return ResponseBuilder.success(
                callRecordingService.getByCall(callPublicId),
                "Call recordings fetched successfully."
        );
    }

    @Operation(summary = "Delete Call Recording")
    @DeleteMapping("/{publicId}")
    @RequiresPermission("CALL_RECORDING_DELETE")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        callRecordingService.delete(publicId);

        return ResponseBuilder.success(
                null,
                CallRecordingMessages.DELETED
        );
    }

    @Operation(summary = "Activate Call Recording")
    @PatchMapping("/{publicId}/activate")
    @RequiresPermission("CALL_RECORDING_UPDATE")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        callRecordingService.activate(publicId);

        return ResponseBuilder.success(
                null,
                CallRecordingMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Call Recording")
    @PatchMapping("/{publicId}/deactivate")
    @RequiresPermission("CALL_RECORDING_UPDATE")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        callRecordingService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                CallRecordingMessages.DEACTIVATED
        );
    }

    @Operation(summary = "Stream Local Call Recording")
    @GetMapping("/{publicId}/stream")
    @RequiresPermission("CALL_RECORDING_VIEW")
    public ResponseEntity<Resource> streamRecording(
            @PathVariable String publicId) {

        Resource resource =
                callRecordingService.getRecordingResource(publicId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline"
                )
                .body(resource);
    }
}