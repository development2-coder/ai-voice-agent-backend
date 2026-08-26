package com.infinitio.aivoiceplatform.organization.organizationbranding.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.organization.organizationbranding.constant.OrganizationBrandingMessages;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.CreateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.request.UpdateOrganizationBrandingRequest;
import com.infinitio.aivoiceplatform.organization.organizationbranding.dto.response.OrganizationBrandingResponse;
import com.infinitio.aivoiceplatform.organization.organizationbranding.service.OrganizationBrandingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organization-brandings")
@Tag(
        name = "Organization Branding",
        description = "Organization Branding Management APIs"
)
public class OrganizationBrandingController {

    private final OrganizationBrandingService organizationBrandingService;

    @Operation(summary = "Create Organization Branding")
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationBrandingResponse>> create(
            @Valid @RequestBody CreateOrganizationBrandingRequest request) {

        log.info("REST Request : Create Organization Branding");

        OrganizationBrandingResponse response =
                organizationBrandingService.create(request);

        return ResponseBuilder.created(
                response,
                OrganizationBrandingMessages.BRANDING_CREATED
        );
    }

    @Operation(summary = "Update Organization Branding")
    @PutMapping
    public ResponseEntity<ApiResponse<OrganizationBrandingResponse>> update(
            @Valid @RequestBody UpdateOrganizationBrandingRequest request) {

        log.info("REST Request : Update Organization Branding");

        OrganizationBrandingResponse response =
                organizationBrandingService.update(request);

        return ResponseBuilder.success(
                response,
                OrganizationBrandingMessages.BRANDING_UPDATED
        );
    }

    @Operation(summary = "Get Organization Branding By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<OrganizationBrandingResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info("REST Request : Get Organization Branding : {}", publicId);

        OrganizationBrandingResponse response =
                organizationBrandingService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Organization Branding fetched successfully."
        );
    }

    @Operation(summary = "Get All Organization Brandings")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrganizationBrandingResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("REST Request : Get All Organization Brandings");

        PageResponse<OrganizationBrandingResponse> response =
                organizationBrandingService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Organization Brandings fetched successfully."
        );
    }

    @Operation(summary = "Delete Organization Branding")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info("REST Request : Delete Organization Branding : {}", publicId);

        organizationBrandingService.delete(publicId);

        return ResponseBuilder.success(
                null,
                OrganizationBrandingMessages.BRANDING_DELETED
        );
    }

    @Operation(summary = "Activate Organization Branding")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info("REST Request : Activate Organization Branding : {}", publicId);

        organizationBrandingService.activate(publicId);

        return ResponseBuilder.success(
                null,
                "Organization Branding activated successfully."
        );
    }

    @Operation(summary = "Deactivate Organization Branding")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info("REST Request : Deactivate Organization Branding : {}", publicId);

        organizationBrandingService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                "Organization Branding deactivated successfully."
        );
    }

}