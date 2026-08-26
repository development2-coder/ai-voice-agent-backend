package com.infinitio.aivoiceplatform.callrecording.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Call Recording Management.
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
    public ResponseEntity<
            ApiResponse<CallRecordingResponse>>
    create(
            @Valid @RequestBody
            CreateCallRecordingRequest request) {

        log.info(
                "REST Request : Create Call Recording"
        );

        CallRecordingResponse response =
                callRecordingService.create(request);

        return ResponseBuilder.created(
                response,
                CallRecordingMessages.CREATED
        );
    }

    @Operation(summary = "Update Call Recording")
    @PutMapping
    public ResponseEntity<
            ApiResponse<CallRecordingResponse>>
    update(
            @Valid @RequestBody
            UpdateCallRecordingRequest request) {

        log.info(
                "REST Request : Update Call Recording"
        );

        CallRecordingResponse response =
                callRecordingService.update(request);

        return ResponseBuilder.success(
                response,
                CallRecordingMessages.UPDATED
        );
    }

    @Operation(summary = "Get Call Recording By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<
            ApiResponse<CallRecordingResponse>>
    getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Call Recording : {}",
                publicId
        );

        CallRecordingResponse response =
                callRecordingService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                "Call recording fetched successfully."
        );
    }

    @Operation(summary = "Get All Call Recordings")
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<CallRecordingResponse>>>
    getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All Call Recordings. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<CallRecordingResponse> response =
                callRecordingService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Call recordings fetched successfully."
        );
    }

    @Operation(summary = "Get Recordings By Call")
    @GetMapping("/call/{callPublicId}")
    public ResponseEntity<
            ApiResponse<PageResponse<CallRecordingResponse>>>
    getByCall(
            @PathVariable String callPublicId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get Recordings By Call : {}",
                callPublicId
        );

        PageResponse<CallRecordingResponse> response =
                callRecordingService.getByCall(
                        callPublicId,
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Call recordings fetched successfully."
        );
    }

    @Operation(summary = "Delete Call Recording")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Call Recording : {}",
                publicId
        );

        callRecordingService.delete(publicId);

        return ResponseBuilder.success(
                null,
                CallRecordingMessages.DELETED
        );
    }

    @Operation(summary = "Activate Call Recording")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Call Recording : {}",
                publicId
        );

        callRecordingService.activate(publicId);

        return ResponseBuilder.success(
                null,
                CallRecordingMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Call Recording")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Call Recording : {}",
                publicId
        );

        callRecordingService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                CallRecordingMessages.DEACTIVATED
        );
    }
}