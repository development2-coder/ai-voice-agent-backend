package com.infinitio.aivoiceplatform.call.controller;

import com.infinitio.aivoiceplatform.call.constant.CallMessages;
import com.infinitio.aivoiceplatform.call.dto.request.CreateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.request.UpdateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.response.CallResponse;
import com.infinitio.aivoiceplatform.call.service.CallService;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.transcript.constant.TranscriptMessages;
import com.infinitio.aivoiceplatform.transcript.dto.response.CallTranscriptResponse;
import com.infinitio.aivoiceplatform.transcript.service.TranscriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    private final TranscriptService transcriptService;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Creates a new call.
     *
     * @param request call creation request
     * @return created call response
     */
    @Operation(summary = "Create Call")
    @PostMapping
    public ResponseEntity<ApiResponse<CallResponse>> create(
            @Valid @RequestBody CreateCallRequest request) {

        log.info("REST Request : Create Call");

        CallResponse response =
                callService.create(request);

        return ResponseEntity
                .status(201)
                .body(
                        ApiResponse.success(
                                CallMessages.CREATED,
                                response
                        )
                );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Updates an existing call.
     *
     * @param request call update request
     * @return updated call response
     */
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

        return ResponseEntity.ok(
                ApiResponse.success(
                        CallMessages.UPDATED,
                        response
                )
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Fetches a call by its public identifier.
     *
     * @param publicId call public identifier
     * @return call response
     */
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

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Call fetched successfully.",
                        response
                )
        );
    }


    /// =========================================================
// GET ALL
// =========================================================

    /**
     * Fetches calls according to the authenticated user's role.
     *
     * <p>
     * SUPER_ADMIN receives calls from all tenants.
     * Other authenticated users receive calls belonging only
     * to their own tenant.
     *
     * @return accessible calls
     */
    @Operation(
            summary = "Get Accessible Calls"
    )
    @GetMapping
    public ResponseEntity<
            ApiResponse<List<CallResponse>>>
    getAll() {

        log.info(
                "REST Request : Get Accessible Calls"
        );

        List<CallResponse> response =
                callService.getAll();

        return ResponseBuilder.success(
                response,
                CallMessages.FETCHED_ALL
        );
    }


    // =========================================================
// GET BY TENANT ID
// =========================================================

    /**
     * Fetches all calls belonging to a selected tenant.
     *
     * <p>
     * This endpoint is restricted to SUPER_ADMIN.
     * Tenant users must use the normal GET calls endpoint,
     * which automatically scopes results to their own tenant.
     *
     * @param tenantId tenant database identifier
     * @return calls belonging to the selected tenant
     */
    @Operation(
            summary = "Get Calls By Tenant Id"
    )
    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<
            ApiResponse<List<CallResponse>>>
    getByTenantId(
            @PathVariable Long tenantId) {

        log.info(
                "REST Request : Get Calls By Tenant."
        );

        List<CallResponse> response =
                callService.getByTenantId(
                        tenantId
                );

        return ResponseBuilder.success(
                response,
                CallMessages.FETCHED_BY_TENANT
        );
    }


    // =========================================================
    // GET CALLS BY CAMPAIGN CONTACT
    // =========================================================

    /**
     * Fetches all calls associated with a campaign contact.
     *
     * <p>
     * No page or size parameters are required. The complete
     * call history for the supplied campaign contact is returned.
     * Tenant filtering is enforced by the service/repository layer.
     *
     * @param campaignContactPublicId campaign contact public identifier
     * @return call history for the campaign contact
     */
    @Operation(
            summary = "Get Calls By Campaign Contact"
    )
    @GetMapping(
            "/campaign-contact/{campaignContactPublicId}"
    )
    public ResponseEntity<
            ApiResponse<List<CallResponse>>>
    getByCampaignContact(
            @PathVariable String campaignContactPublicId) {

        log.info(
                "REST Request : Get Calls By Campaign Contact : {}",
                campaignContactPublicId
        );

        List<CallResponse> response =
                callService.getByCampaignContact(
                        campaignContactPublicId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Calls fetched successfully.",
                        response
                )
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Soft deletes a call.
     *
     * @param publicId call public identifier
     * @return empty success response
     */
    @Operation(summary = "Delete Call")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Call : {}",
                publicId
        );

        callService.delete(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        CallMessages.DELETED,
                        null
                )
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activates a call.
     *
     * @param publicId call public identifier
     * @return activation response
     */
    @Operation(summary = "Activate Call")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Call : {}",
                publicId
        );

        callService.activate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Call activated successfully.",
                        null
                )
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivates a call.
     *
     * @param publicId call public identifier
     * @return deactivation response
     */
    @Operation(summary = "Deactivate Call")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Call : {}",
                publicId
        );

        callService.deactivate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Call deactivated successfully.",
                        null
                )
        );
    }


    // =========================================================
    // GET COMPLETE CALL TRANSCRIPT
    // =========================================================

    /**
     * Fetches the complete transcript for a call.
     *
     * @param publicId call public identifier
     * @return complete call transcript
     */
    @Operation(
            summary = "Get Complete Call Transcript"
    )
    @GetMapping("/{publicId}/transcript")
    public ResponseEntity<
            ApiResponse<CallTranscriptResponse>>
    getTranscript(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Complete Call Transcript : {}",
                publicId
        );

        CallTranscriptResponse response =
                transcriptService
                        .getCompleteCallTranscript(
                                publicId
                        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        TranscriptMessages
                                .COMPLETE_TRANSCRIPT_FETCHED,
                        response
                )
        );
    }
}