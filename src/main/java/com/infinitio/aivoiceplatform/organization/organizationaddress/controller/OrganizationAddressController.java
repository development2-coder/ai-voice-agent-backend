package com.infinitio.aivoiceplatform.organization.organizationaddress.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.organization.organizationaddress.constant.OrganizationAddressMessages;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.CreateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.request.UpdateOrganizationAddressRequest;
import com.infinitio.aivoiceplatform.organization.organizationaddress.dto.response.OrganizationAddressResponse;
import com.infinitio.aivoiceplatform.organization.organizationaddress.service.OrganizationAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Organization Address Controller.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organization-addresses")
@Tag(
        name = "Organization Address",
        description = "Organization Address Management APIs"
)
public class OrganizationAddressController {

    private final OrganizationAddressService organizationAddressService;

    /**
     * Create Organization Address.
     */
    @Operation(summary = "Create Organization Address")
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationAddressResponse>> create(
            @Valid @RequestBody CreateOrganizationAddressRequest request) {

        log.info("REST Request : Create Organization Address");

        OrganizationAddressResponse response =
                organizationAddressService.create(request);

        return ResponseBuilder.created(
                response,
                OrganizationAddressMessages.ADDRESS_CREATED
        );
    }

    /**
     * Update Organization Address.
     */
    @Operation(summary = "Update Organization Address")
    @PutMapping
    public ResponseEntity<ApiResponse<OrganizationAddressResponse>> update(
            @Valid @RequestBody UpdateOrganizationAddressRequest request) {

        log.info("REST Request : Update Organization Address");

        OrganizationAddressResponse response =
                organizationAddressService.update(request);

        return ResponseBuilder.success(
                response,
                OrganizationAddressMessages.ADDRESS_UPDATED
        );
    }

    /**
     * Get Organization Address By Public Id.
     */
    @Operation(summary = "Get Organization Address By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<OrganizationAddressResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info("REST Request : Get Organization Address : {}", publicId);

        OrganizationAddressResponse response =
                organizationAddressService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Organization Address fetched successfully."
        );
    }

    /**
     * Get All Organization Addresses.
     */
    @Operation(summary = "Get All Organization Addresses")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrganizationAddressResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("REST Request : Get All Organization Addresses");

        PageResponse<OrganizationAddressResponse> response =
                organizationAddressService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Organization Addresses fetched successfully."
        );
    }

    /**
     * Delete Organization Address.
     */
    @Operation(summary = "Delete Organization Address")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info("REST Request : Delete Organization Address : {}", publicId);

        organizationAddressService.delete(publicId);

        return ResponseBuilder.success(
                null,
                OrganizationAddressMessages.ADDRESS_DELETED
        );
    }

    /**
     * Activate Organization Address.
     */
    @Operation(summary = "Activate Organization Address")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info("REST Request : Activate Organization Address : {}", publicId);

        organizationAddressService.activate(publicId);

        return ResponseBuilder.success(
                null,
                "Organization Address activated successfully."
        );
    }

    /**
     * Deactivate Organization Address.
     */
    @Operation(summary = "Deactivate Organization Address")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info("REST Request : Deactivate Organization Address : {}", publicId);

        organizationAddressService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                "Organization Address deactivated successfully."
        );
    }

}