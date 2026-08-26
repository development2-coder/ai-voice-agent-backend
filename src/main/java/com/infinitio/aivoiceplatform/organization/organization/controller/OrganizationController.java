package com.infinitio.aivoiceplatform.organization.organization.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.organization.constant.OrganizationMessages;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.CreateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.request.UpdateOrganizationRequest;
import com.infinitio.aivoiceplatform.organization.organization.dto.response.OrganizationResponse;
import com.infinitio.aivoiceplatform.organization.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Organization Controller.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<
            ApiResponse<OrganizationResponse>> create(
            @Valid
            @RequestBody
            CreateOrganizationRequest request) {

        log.info(
                "REST Request : Create Organization"
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationMessages.CREATED,
                        organizationService.create(request)
                )
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping
    public ResponseEntity<
            ApiResponse<OrganizationResponse>> update(
            @Valid
            @RequestBody
            UpdateOrganizationRequest request) {

        log.info(
                "REST Request : Update Organization | publicId={}",
                request.getPublicId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationMessages.UPDATED,
                        organizationService.update(request)
                )
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<OrganizationResponse>>> getAll(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size) {

        log.info(
                "REST Request : Get All Organizations | page={} | size={}",
                page,
                size
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationMessages.LIST_RETRIEVED,
                        organizationService.getAll(
                                page,
                                size
                        )
                )
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @GetMapping("/{publicId}")
    public ResponseEntity<
            ApiResponse<OrganizationResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Organization | publicId={}",
                publicId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationMessages.RETRIEVED,
                        organizationService.getByPublicId(
                                publicId
                        )
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
                "REST Request : Delete Organization | publicId={}",
                publicId
        );

        organizationService.delete(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationMessages.DELETED,
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
                "REST Request : Activate Organization | publicId={}",
                publicId
        );

        organizationService.activate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationMessages.ACTIVATED,
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
                "REST Request : Deactivate Organization | publicId={}",
                publicId
        );

        organizationService.deactivate(publicId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        OrganizationMessages.DEACTIVATED,
                        null
                )
        );
    }
}