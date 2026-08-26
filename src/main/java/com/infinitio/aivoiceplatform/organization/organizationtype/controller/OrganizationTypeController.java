package com.infinitio.aivoiceplatform.organization.organizationtype.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organizationtype.constant.OrganizationTypeMessages;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.CreateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.request.UpdateOrganizationTypeRequest;
import com.infinitio.aivoiceplatform.organization.organizationtype.dto.response.OrganizationTypeResponse;
import com.infinitio.aivoiceplatform.organization.organizationtype.service.OrganizationTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Organization Type Controller.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organization-types")
@RequiredArgsConstructor
public class OrganizationTypeController {

    private final OrganizationTypeService service;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<
            ApiResponse<OrganizationTypeResponse>> create(
            @Valid
            @RequestBody
            CreateOrganizationTypeRequest request) {

        log.info(
                "REST Request : Create Organization Type"
        );

        OrganizationTypeResponse response =
                service.create(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationTypeMessages.CREATED,
                        response
                )
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping
    public ResponseEntity<
            ApiResponse<OrganizationTypeResponse>> update(
            @Valid
            @RequestBody
            UpdateOrganizationTypeRequest request) {

        log.info(
                "REST Request : Update Organization Type | publicId={}",
                request.getPublicId()
        );

        OrganizationTypeResponse response =
                service.update(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationTypeMessages.UPDATED,
                        response
                )
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @GetMapping("/{publicId}")
    public ResponseEntity<
            ApiResponse<OrganizationTypeResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Organization Type | publicId={}",
                publicId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organization type retrieved successfully.",
                        service.getByPublicId(publicId)
                )
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<OrganizationTypeResponse>>> getAll(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size) {

        log.info(
                "REST Request : Get All Organization Types | page={} | size={}",
                page,
                size
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organization types retrieved successfully.",
                        service.getAll(page, size)
                )
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Organization Type | publicId={}",
                publicId
        );

        service.delete(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationTypeMessages.DELETED,
                        null
                )
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Organization Type | publicId={}",
                publicId
        );

        service.activate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationTypeMessages.ACTIVATED,
                        null
                )
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Organization Type | publicId={}",
                publicId
        );

        service.deactivate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationTypeMessages.DEACTIVATED,
                        null
                )
        );
    }
}