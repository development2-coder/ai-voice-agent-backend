package com.infinitio.aivoiceplatform.master.permission.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.master.permission.constant.PermissionMessages;
import com.infinitio.aivoiceplatform.master.permission.dto.request.CreatePermissionRequest;
import com.infinitio.aivoiceplatform.master.permission.dto.request.UpdatePermissionRequest;
import com.infinitio.aivoiceplatform.master.permission.dto.response.PermissionResponse;
import com.infinitio.aivoiceplatform.master.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Permission Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/permissions")
@Tag(
        name = "Permission",
        description = "Permission Management APIs"
)
public class PermissionController {

    private final PermissionService permissionService;


    // =========================================================
    // CREATE
    // =========================================================

    @Operation(
            summary = "Create Permission"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> create(
            @Valid @RequestBody CreatePermissionRequest request) {

        log.info("REST Request : Create Permission");

        PermissionResponse response =
                permissionService.create(request);

        return ResponseBuilder.created(
                response,
                PermissionMessages.CREATED
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Operation(
            summary = "Update Permission"
    )
    @PutMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @Valid @RequestBody UpdatePermissionRequest request) {

        log.info("REST Request : Update Permission");

        PermissionResponse response =
                permissionService.update(request);

        return ResponseBuilder.success(
                response,
                PermissionMessages.UPDATED
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Operation(
            summary = "Get Permission By Public Id"
    )
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Permission By Public Id : {}",
                publicId
        );

        PermissionResponse response =
                permissionService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Permission fetched successfully."
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Operation(
            summary = "Get All Permissions"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info(
                "REST Request : Get All Permissions. Page : {}, Size : {}",
                page,
                size
        );

        PageResponse<PermissionResponse> response =
                permissionService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Permissions fetched successfully."
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Operation(
            summary = "Delete Permission"
    )
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Permission : {}",
                publicId
        );

        permissionService.delete(
                publicId
        );

        return ResponseBuilder.success(
                null,
                PermissionMessages.DELETED
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Operation(
            summary = "Activate Permission"
    )
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Permission : {}",
                publicId
        );

        permissionService.activate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                PermissionMessages.ACTIVATED
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Operation(
            summary = "Deactivate Permission"
    )
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Permission : {}",
                publicId
        );

        permissionService.deactivate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                PermissionMessages.DEACTIVATED
        );
    }
}