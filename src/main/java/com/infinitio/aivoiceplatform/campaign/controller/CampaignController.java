package com.infinitio.aivoiceplatform.campaign.controller;

import com.infi.library.annotation.RequiresPermission;
import com.infinitio.aivoiceplatform.campaign.constant.CampaignMessages;
import com.infinitio.aivoiceplatform.campaign.dto.request.CreateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.request.UpdateCampaignRequest;
import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignResponse;
import com.infinitio.aivoiceplatform.campaign.dto.response.CampaignVariablesResponse;
import com.infinitio.aivoiceplatform.campaign.service.CampaignService;
import com.infinitio.aivoiceplatform.campaign.service.CampaignVariableService;
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

    private final CampaignVariableService
            campaignVariableService;


    /**
     * Creates a Campaign.
     *
     * @param request create request
     * @return created campaign
     */
    @Operation(summary = "Create Campaign")
    @RequiresPermission("CAMPAIGN_CREATE")
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


    /**
     * Updates a Campaign.
     *
     * @param request update request
     * @return updated campaign
     */
    @Operation(summary = "Update Campaign")
    @RequiresPermission("CAMPAIGN_UPDATE")
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


    /**
     * Gets a Campaign by public ID.
     *
     * @param publicId campaign public ID
     * @return campaign
     */
    @Operation(summary = "Get Campaign By Public Id")
    @RequiresPermission("CAMPAIGN_VIEW")
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


    /**
     * Gets Campaign Flow variables.
     *
     * @param publicId campaign public ID
     * @return campaign variables
     */
    @Operation(
            summary = "Get Campaign Flow Variables"
    )
    @RequiresPermission("CAMPAIGN_VIEW")
    @GetMapping("/{publicId}/variables")
    public ResponseEntity<
            ApiResponse<CampaignVariablesResponse>>
    getVariables(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Campaign Flow Variables : {}",
                publicId
        );

        CampaignVariablesResponse response =
                campaignVariableService.getVariables(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                "Campaign Flow variables fetched successfully."
        );
    }


    /**
     * Gets all campaigns.
     *
     * @return campaigns visible to current user
     */
    @Operation(summary = "Get All Campaigns")
    @RequiresPermission("CAMPAIGN_VIEW")
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<CampaignResponse>>>
    getAll() {

        log.info(
                "REST Request : Get All Campaigns"
        );

        PageResponse<CampaignResponse> response =
                campaignService.getAll();

        return ResponseBuilder.success(
                response,
                "Campaigns fetched successfully."
        );
    }


    /**
     * Deletes a Campaign.
     *
     * @param publicId campaign public ID
     * @return success response
     */
    @Operation(summary = "Delete Campaign")
    @RequiresPermission("CAMPAIGN_DELETE")
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


    /**
     * Activates a Campaign.
     *
     * @param publicId campaign public ID
     * @return success response
     */
    @Operation(summary = "Activate Campaign")
    @RequiresPermission("CAMPAIGN_UPDATE")
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


    /**
     * Deactivates a Campaign.
     *
     * @param publicId campaign public ID
     * @return success response
     */
    @Operation(summary = "Deactivate Campaign")
    @RequiresPermission("CAMPAIGN_UPDATE")
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