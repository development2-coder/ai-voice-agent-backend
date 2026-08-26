package com.infinitio.aivoiceplatform.master.platformmodule.controller;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.CreatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.request.UpdatePlatformModuleRequest;
import com.infinitio.aivoiceplatform.master.platformmodule.dto.response.PlatformModuleResponse;
import com.infinitio.aivoiceplatform.master.platformmodule.service.PlatformModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Platform Module Controller.
 *
 * Provides REST APIs for managing platform modules.
 *
 * Endpoints:
 *
 * POST   /api/v1/platform-modules
 * PUT    /api/v1/platform-modules
 * GET    /api/v1/platform-modules/{publicId}
 * GET    /api/v1/platform-modules
 * DELETE /api/v1/platform-modules/{publicId}
 * PATCH  /api/v1/platform-modules/{publicId}/activate
 * PATCH  /api/v1/platform-modules/{publicId}/deactivate
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/platform-modules")
@RequiredArgsConstructor
public class PlatformModuleController {


    // =========================================================
    // SERVICE
    // =========================================================

    private final PlatformModuleService platformModuleService;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Create a new Platform Module.
     *
     * @param request create request
     * @return created Platform Module
     */
    @PostMapping
    public ResponseEntity<PlatformModuleResponse> create(
            @Valid
            @RequestBody
            CreatePlatformModuleRequest request) {

        log.info(
                "REST Request : Create Platform Module | moduleCode={}",
                request.getModuleCode()
        );

        PlatformModuleResponse response =
                platformModuleService.create(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Update an existing Platform Module.
     *
     * @param request update request
     * @return updated Platform Module
     */
    @PutMapping
    public ResponseEntity<PlatformModuleResponse> update(
            @Valid
            @RequestBody
            UpdatePlatformModuleRequest request) {

        log.info(
                "REST Request : Update Platform Module | publicId={}",
                request.getPublicId()
        );

        PlatformModuleResponse response =
                platformModuleService.update(
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Get Platform Module by public ID.
     *
     * @param publicId public UUID
     * @return Platform Module
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<PlatformModuleResponse> getByPublicId(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Get Platform Module | publicId={}",
                publicId
        );

        PlatformModuleResponse response =
                platformModuleService.getByPublicId(
                        publicId
                );

        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Get all non-deleted Platform Modules.
     *
     * Pagination:
     *
     * page = zero-based page number
     * size = number of records per page
     *
     * @param page page number
     * @param size page size
     * @return paginated Platform Modules
     */
    @GetMapping
    public ResponseEntity<PageResponse<PlatformModuleResponse>> getAll(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size) {

        log.info(
                "REST Request : Get All Platform Modules | page={} | size={}",
                page,
                size
        );

        PageResponse<PlatformModuleResponse> response =
                platformModuleService.getAll(
                        page,
                        size
                );

        return ResponseEntity.ok(
                response
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Soft delete a Platform Module.
     *
     * @param publicId public UUID
     * @return no content
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Delete Platform Module | publicId={}",
                publicId
        );

        platformModuleService.delete(
                publicId
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activate a Platform Module.
     *
     * @param publicId public UUID
     * @return no content
     */
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Activate Platform Module | publicId={}",
                publicId
        );

        platformModuleService.activate(
                publicId
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivate a Platform Module.
     *
     * @param publicId public UUID
     * @return no content
     */
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Deactivate Platform Module | publicId={}",
                publicId
        );

        platformModuleService.deactivate(
                publicId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}