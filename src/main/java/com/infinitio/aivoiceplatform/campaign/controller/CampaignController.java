package com.infinitio.aivoiceplatform.campaign.controller;

import com.infinitio.aivoiceplatform.campaign.constant.CampaignMessages;
import com.infinitio.aivoiceplatform.campaign.dto.request.CreateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.request.UpdateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignResponse;
import com.infinitio.aivoiceplatform.campaign.service.CampaignService;
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
 * REST Controller for Campaign Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campaigns")
@Tag(
        name = "Campaign",
        description = "AI Voice Campaign Management APIs"
)
public class CampaignController {

    private final CampaignService campaignService;

    @Operation(summary = "Create Campaign")
    @PostMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> create(
            @Valid @RequestBody
            CreateCampaignRequest request) {

        log.info(
                "REST Request : Create Campaign"
        );

        CampaignResponse response =
                campaignService.create(request);

        return ResponseBuilder.created(
                response,
                CampaignMessages.CREATED
        );
    }

    @Operation(summary = "Update Campaign")
    @PutMapping
    public ResponseEntity<ApiResponse<CampaignResponse>> update(
            @Valid @RequestBody
            UpdateCampaignRequest request) {

        log.info(
                "REST Request : Update Campaign"
        );

        CampaignResponse response =
                campaignService.update(request);

        return ResponseBuilder.success(
                response,
                CampaignMessages.UPDATED
        );
    }

    @Operation(summary = "Get Campaign By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<CampaignResponse>>
    getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Campaign : {}",
                publicId
        );

        CampaignResponse response =
                campaignService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                "Campaign fetched successfully."
        );
    }

    @Operation(summary = "Get All Campaigns")
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<CampaignResponse>>>
    getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info(
                "REST Request : Get All Campaigns. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<CampaignResponse> response =
                campaignService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Campaigns fetched successfully."
        );
    }

    @Operation(summary = "Delete Campaign")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Campaign : {}",
                publicId
        );

        campaignService.delete(publicId);

        return ResponseBuilder.success(
                null,
                CampaignMessages.DELETED
        );
    }

    @Operation(summary = "Activate Campaign")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Campaign : {}",
                publicId
        );

        campaignService.activate(publicId);

        return ResponseBuilder.success(
                null,
                CampaignMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Campaign")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Campaign : {}",
                publicId
        );

        campaignService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                CampaignMessages.DEACTIVATED
        );
    }
}