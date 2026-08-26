package com.infinitio.aivoiceplatform.organization.organizationstatus.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organizationstatus.constant.OrganizationStatusMessages;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.CreateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.request.UpdateOrganizationStatusRequest;
import com.infinitio.aivoiceplatform.organization.organizationstatus.dto.response.OrganizationStatusResponse;
import com.infinitio.aivoiceplatform.organization.organizationstatus.service.OrganizationStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Organization Status Controller.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organization-statuses")
@RequiredArgsConstructor
public class OrganizationStatusController {

    private final OrganizationStatusService service;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<
            ApiResponse<OrganizationStatusResponse>> create(
            @Valid
            @RequestBody
            CreateOrganizationStatusRequest request) {

        log.info(
                "REST Request : Create Organization Status"
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationStatusMessages.CREATED,
                        service.create(request)
                )
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping
    public ResponseEntity<
            ApiResponse<OrganizationStatusResponse>> update(
            @Valid
            @RequestBody
            UpdateOrganizationStatusRequest request) {

        log.info(
                "REST Request : Update Organization Status | publicId={}",
                request.getPublicId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationStatusMessages.UPDATED,
                        service.update(request)
                )
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @GetMapping("/{publicId}")
    public ResponseEntity<
            ApiResponse<OrganizationStatusResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Organization Status | publicId={}",
                publicId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organization status retrieved successfully.",
                        service.getByPublicId(publicId)
                )
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<OrganizationStatusResponse>>> getAll(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size) {

        log.info(
                "REST Request : Get All Organization Statuses | page={} | size={}",
                page,
                size
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Organization statuses retrieved successfully.",
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
                "REST Request : Delete Organization Status | publicId={}",
                publicId
        );

        service.delete(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationStatusMessages.DELETED,
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
                "REST Request : Activate Organization Status | publicId={}",
                publicId
        );

        service.activate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationStatusMessages.ACTIVATED,
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
                "REST Request : Deactivate Organization Status | publicId={}",
                publicId
        );

        service.deactivate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationStatusMessages.DEACTIVATED,
                        null
                )
        );
    }
}