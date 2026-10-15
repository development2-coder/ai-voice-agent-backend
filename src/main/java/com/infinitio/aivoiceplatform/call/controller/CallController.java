package com.infinitio.aivoiceplatform.call.controller;

import com.infi.library.annotation.RequiresPermission;
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
 * REST controller for call management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calls")
@Tag(name = "Call", description = "AI Voice Call Management APIs")
public class CallController {

    private final CallService callService;
    private final TranscriptService transcriptService;

    @Operation(summary = "Create Call")
    @PostMapping
    @RequiresPermission("CALL_CREATE")
    public ResponseEntity<ApiResponse<CallResponse>> create(
            @Valid @RequestBody CreateCallRequest request) {

        CallResponse response = callService.create(request);

        return ResponseEntity.status(201).body(
                ApiResponse.success(CallMessages.CREATED, response)
        );
    }

    @Operation(summary = "Update Call")
    @PutMapping
    @RequiresPermission("CALL_UPDATE")
    public ResponseEntity<ApiResponse<CallResponse>> update(
            @Valid @RequestBody UpdateCallRequest request) {

        CallResponse response = callService.update(request);

        return ResponseEntity.ok(
                ApiResponse.success(CallMessages.UPDATED, response)
        );
    }

    @Operation(summary = "Get Call By Public ID")
    @GetMapping("/{publicId}")
    @RequiresPermission("CALL_VIEW")
    public ResponseEntity<ApiResponse<CallResponse>> getByPublicId(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        CallMessages.FETCHED,
                        callService.getByPublicId(publicId)
                )
        );
    }

    @Operation(summary = "Get Accessible Calls")
    @GetMapping
    @RequiresPermission("CALL_VIEW")
    public ResponseEntity<ApiResponse<List<CallResponse>>> getAll() {

        return ResponseBuilder.success(
                callService.getAll(),
                CallMessages.FETCHED_ALL
        );
    }

    @Operation(summary = "Get Calls By Tenant ID")
    @GetMapping("/tenants/{tenantId}")
    @RequiresPermission("CALL_VIEW")
    public ResponseEntity<ApiResponse<List<CallResponse>>> getByTenantId(
            @PathVariable Long tenantId) {

        return ResponseBuilder.success(
                callService.getByTenantId(tenantId),
                CallMessages.FETCHED_BY_TENANT
        );
    }

    @Operation(summary = "Get Calls By Campaign Contact")
    @GetMapping("/campaign-contact/{campaignContactPublicId}")
    @RequiresPermission("CALL_VIEW")
    public ResponseEntity<ApiResponse<List<CallResponse>>> getByCampaignContact(
            @PathVariable String campaignContactPublicId) {

        return ResponseBuilder.success(
                callService.getByCampaignContact(campaignContactPublicId),
                CallMessages.FETCHED_ALL
        );
    }

    @Operation(summary = "Delete Call")
    @DeleteMapping("/{publicId}")
    @RequiresPermission("CALL_DELETE")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        callService.delete(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(CallMessages.DELETED, null)
        );
    }

    @Operation(summary = "Activate Call")
    @PatchMapping("/{publicId}/activate")
    @RequiresPermission("CALL_UPDATE")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        callService.activate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success("Call activated successfully.", null)
        );
    }

    @Operation(summary = "Deactivate Call")
    @PatchMapping("/{publicId}/deactivate")
    @RequiresPermission("CALL_UPDATE")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        callService.deactivate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success("Call deactivated successfully.", null)
        );
    }

    @Operation(summary = "Get Complete Call Transcript")
    @GetMapping("/{publicId}/transcript")
    @RequiresPermission("TRANSCRIPT_VIEW")
    public ResponseEntity<ApiResponse<CallTranscriptResponse>> getTranscript(
            @PathVariable String publicId) {

        CallTranscriptResponse response =
                transcriptService.getCompleteCallTranscript(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        TranscriptMessages.COMPLETE_TRANSCRIPT_FETCHED,
                        response
                )
        );
    }

    @Operation(summary = "Get Calls By Campaign")
    @GetMapping("/campaign/{campaignPublicId}")
    @RequiresPermission("CALL_VIEW")
    public ResponseEntity<ApiResponse<List<CallResponse>>> getByCampaign(
            @PathVariable String campaignPublicId) {

        return ResponseBuilder.success(
                callService.getByCampaign(campaignPublicId),
                "Campaign calls fetched successfully."
        );
    }
}