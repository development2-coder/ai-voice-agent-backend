package com.infinitio.aivoiceplatform.organization.tenant.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.organization.tenant.constant.TenantMessages;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.CreateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.request.UpdateTenantRequest;
import com.infinitio.aivoiceplatform.organization.tenant.dto.response.TenantResponse;
import com.infinitio.aivoiceplatform.organization.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Tenant Controller.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> create(
            @Valid
            @RequestBody
            CreateTenantRequest request) {

        log.info(
                "REST Request : Create Tenant"
        );

        TenantResponse response =
                tenantService.create(
                        request
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        ApiResponse
                                .<TenantResponse>builder()
                                .statusCode(
                                        HttpStatus.CREATED.value()
                                )
                                .message(
                                        TenantMessages.TENANT_CREATED
                                )
                                .data(
                                        response
                                )
                                .build()
                );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping
    public ResponseEntity<ApiResponse<TenantResponse>> update(
            @Valid
            @RequestBody
            UpdateTenantRequest request) {

        log.info(
                "REST Request : Update Tenant | publicId={}",
                request.getPublicId()
        );

        TenantResponse response =
                tenantService.update(
                        request
                );

        return ResponseEntity.ok(
                ApiResponse
                        .<TenantResponse>builder()
                        .statusCode(
                                HttpStatus.OK.value()
                        )
                        .message(
                                TenantMessages.TENANT_UPDATED
                        )
                        .data(
                                response
                        )
                        .build()
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<TenantResponse>>
    getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Tenant | publicId={}",
                publicId
        );

        TenantResponse response =
                tenantService.getByPublicId(
                        publicId
                );

        return ResponseEntity.ok(
                ApiResponse
                        .<TenantResponse>builder()
                        .statusCode(
                                HttpStatus.OK.value()
                        )
                        .message(
                                "Tenant retrieved successfully."
                        )
                        .data(
                                response
                        )
                        .build()
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<TenantResponse>>>
    getAll(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size) {

        PageResponse<TenantResponse> response =
                tenantService.getAll(
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse
                        .<PageResponse<TenantResponse>>builder()
                        .statusCode(
                                HttpStatus.OK.value()
                        )
                        .message(
                                "Tenants retrieved successfully."
                        )
                        .data(
                                response
                        )
                        .build()
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        tenantService.delete(
                publicId
        );

        return ResponseEntity.ok(
                ApiResponse
                        .<Void>builder()
                        .statusCode(
                                HttpStatus.OK.value()
                        )
                        .message(
                                TenantMessages.TENANT_DELETED
                        )
                        .build()
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        tenantService.activate(
                publicId
        );

        return ResponseEntity.ok(
                ApiResponse
                        .<Void>builder()
                        .statusCode(
                                HttpStatus.OK.value()
                        )
                        .message(
                                TenantMessages.TENANT_ACTIVATED
                        )
                        .build()
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        tenantService.deactivate(
                publicId
        );

        return ResponseEntity.ok(
                ApiResponse
                        .<Void>builder()
                        .statusCode(
                                HttpStatus.OK.value()
                        )
                        .message(
                                TenantMessages.TENANT_DEACTIVATED
                        )
                        .build()
        );
    }
}