package com.infinitio.aivoiceplatform.campaigncontact.controller;

import com.infinitio.aivoiceplatform.campaigncontact.constant.CampaignContactMessages;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.CreateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.request.UpdateCampaignContactRequest;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactExcelUploadResponse;
import com.infinitio.aivoiceplatform.campaigncontact.dto.response.CampaignContactResponse;
import com.infinitio.aivoiceplatform.campaigncontact.service.CampaignContactService;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for Campaign Contact Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/campaign-contacts")
@Tag(
        name = "Campaign Contact",
        description = "Campaign Contact Management APIs"
)
public class CampaignContactController {

    private final CampaignContactService
            campaignContactService;

    @Operation(summary = "Create Campaign Contact")
    @PostMapping
    public ResponseEntity<
            ApiResponse<CampaignContactResponse>>
    create(
            @Valid @RequestBody
            CreateCampaignContactRequest request) {

        CampaignContactResponse response =
                campaignContactService.create(
                        request
                );

        return ResponseBuilder.created(
                response,
                CampaignContactMessages.CREATED
        );
    }

    @Operation(summary = "Update Campaign Contact")
    @PutMapping
    public ResponseEntity<
            ApiResponse<CampaignContactResponse>>
    update(
            @Valid @RequestBody
            UpdateCampaignContactRequest request) {

        CampaignContactResponse response =
                campaignContactService.update(
                        request
                );

        return ResponseBuilder.success(
                response,
                CampaignContactMessages.UPDATED
        );
    }

    @Operation(
            summary = "Upload Campaign Contacts Excel"
    )
    @PostMapping(
            value = "/upload",
            consumes =
                    MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<
            ApiResponse<
                    CampaignContactExcelUploadResponse>>
    uploadExcel(
            @RequestParam
            String campaignPublicId,
            @RequestPart
            MultipartFile file) {

        log.info(
                "REST Request : Upload Campaign Contacts Excel. "
                        + "Campaign : {}",
                campaignPublicId
        );

        CampaignContactExcelUploadResponse response =
                campaignContactService.uploadExcel(
                        campaignPublicId,
                        file
                );

        return ResponseBuilder.success(
                response,
                CampaignContactMessages
                        .EXCEL_UPLOAD_COMPLETED
        );
    }

    @Operation(summary = "Get Campaign Contact")
    @GetMapping("/{publicId}")
    public ResponseEntity<
            ApiResponse<CampaignContactResponse>>
    getByPublicId(
            @PathVariable String publicId) {

        CampaignContactResponse response =
                campaignContactService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                "Campaign contact fetched successfully."
        );
    }

    @Operation(summary = "Get All Campaign Contacts")
    @GetMapping
    public ResponseEntity<
            ApiResponse<
                    PageResponse<CampaignContactResponse>>>
    getAll(
            @RequestParam(defaultValue = "0")
            Integer page,
            @RequestParam(defaultValue = "10")
            Integer size) {

        PageResponse<CampaignContactResponse> response =
                campaignContactService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Campaign contacts fetched successfully."
        );
    }

    @Operation(summary = "Get Contacts By Campaign")
    @GetMapping(
            "/campaign/{campaignPublicId}"
    )
    public ResponseEntity<
            ApiResponse<
                    PageResponse<CampaignContactResponse>>>
    getByCampaign(
            @PathVariable String campaignPublicId,
            @RequestParam(defaultValue = "0")
            Integer page,
            @RequestParam(defaultValue = "10")
            Integer size) {

        PageResponse<CampaignContactResponse> response =
                campaignContactService.getByCampaign(
                        campaignPublicId,
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Campaign contacts fetched successfully."
        );
    }

    @Operation(summary = "Delete Campaign Contact")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>>
    delete(
            @PathVariable String publicId) {

        campaignContactService.delete(
                publicId
        );

        return ResponseBuilder.success(
                null,
                CampaignContactMessages.DELETED
        );
    }

    @Operation(summary = "Activate Campaign Contact")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>>
    activate(
            @PathVariable String publicId) {

        campaignContactService.activate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                CampaignContactMessages.ACTIVATED
        );
    }

    @Operation(summary = "Deactivate Campaign Contact")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>>
    deactivate(
            @PathVariable String publicId) {

        campaignContactService.deactivate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                CampaignContactMessages.DEACTIVATED
        );
    }
}