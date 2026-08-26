package com.infinitio.aivoiceplatform.call.controller;

import com.infinitio.aivoiceplatform.call.constant.CallMessages;
import com.infinitio.aivoiceplatform.call.dto.request.CreateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.request.UpdateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.response.CallResponse;
import com.infinitio.aivoiceplatform.call.service.CallService;
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
 * REST Controller for Call Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calls")
@Tag(
        name = "Call",
        description = "AI Voice Call Management APIs"
)
public class CallController {

    private final CallService callService;


    // =========================================================
    // CREATE
    // =========================================================

    @Operation(summary = "Create Call")
    @PostMapping
    public ResponseEntity<ApiResponse<CallResponse>> create(
            @Valid @RequestBody CreateCallRequest request) {

        log.info("REST Request : Create Call");

        CallResponse response =
                callService.create(request);

        return ResponseBuilder.created(
                response,
                CallMessages.CREATED
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Operation(summary = "Update Call")
    @PutMapping
    public ResponseEntity<ApiResponse<CallResponse>> update(
            @Valid @RequestBody UpdateCallRequest request) {

        log.info(
                "REST Request : Update Call | Public Id : {}",
                request.getPublicId()
        );

        CallResponse response =
                callService.update(request);

        return ResponseBuilder.success(
                response,
                CallMessages.UPDATED
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Operation(summary = "Get Call By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<CallResponse>>
    getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Call : {}",
                publicId
        );

        CallResponse response =
                callService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                "Call fetched successfully."
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Operation(summary = "Get All Calls")
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<CallResponse>>>
    getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All Calls. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<CallResponse> response =
                callService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Calls fetched successfully."
        );
    }


    // =========================================================
    // GET BY CAMPAIGN CONTACT
    // =========================================================

    @Operation(
            summary = "Get Calls By Campaign Contact"
    )
    @GetMapping(
            "/campaign-contact/{campaignContactPublicId}"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<CallResponse>>>
    getByCampaignContact(
            @PathVariable String campaignContactPublicId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get Calls By Campaign Contact : {}",
                campaignContactPublicId
        );

        PageResponse<CallResponse> response =
                callService.getByCampaignContact(
                        campaignContactPublicId,
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Calls fetched successfully."
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Operation(summary = "Delete Call")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Call : {}",
                publicId
        );

        callService.delete(publicId);

        return ResponseBuilder.success(
                null,
                CallMessages.DELETED
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Operation(summary = "Activate Call")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Call : {}",
                publicId
        );

        callService.activate(publicId);

        return ResponseBuilder.success(
                null,
                "Call activated successfully."
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Operation(summary = "Deactivate Call")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Call : {}",
                publicId
        );

        callService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                "Call deactivated successfully."
        );
    }
}