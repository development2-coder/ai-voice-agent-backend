package com.infinitio.aivoiceplatform.master.role.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.master.role.constant.RoleMessages;
import com.infinitio.aivoiceplatform.master.role.dto.request.CreateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.request.UpdateRoleRequest;
import com.infinitio.aivoiceplatform.master.role.dto.response.RoleResponse;
import com.infinitio.aivoiceplatform.master.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Role Controller.
 *
 * <p>
 * Provides REST APIs for role management.
 * </p>
 *
 * <p>
 * Role administration is protected through the common library
 * RBAC permission framework. Additional role hierarchy rules
 * are enforced inside the service layer.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
@Tag(
        name = "Role",
        description = "Role Management APIs"
)
public class RoleController {

    private final RoleService roleService;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Create Role.
     *
     * @param request role creation request
     * @return created role
     */
    @Operation(summary = "Create Role")
    @PostMapping
    @PreAuthorize("hasPermission(null, 'ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Valid @RequestBody CreateRoleRequest request) {

        log.info("REST Request : Create Role");

        RoleResponse response =
                roleService.create(request);

        return ResponseBuilder.created(
                response,
                RoleMessages.ROLE_CREATED
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Update Role.
     *
     * @param request role update request
     * @return updated role
     */
    @Operation(summary = "Update Role")
    @PutMapping
    @PreAuthorize("hasPermission(null, 'ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("REST Request : Update Role");

        RoleResponse response =
                roleService.update(request);

        return ResponseBuilder.success(
                response,
                RoleMessages.ROLE_UPDATED
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Get Role By Public Id.
     *
     * @param publicId role public ID
     * @return role
     */
    @Operation(summary = "Get Role By Public Id")
    @GetMapping("/{publicId}")
    @PreAuthorize("hasPermission(null, 'ROLE_VIEW')")
    public ResponseEntity<ApiResponse<RoleResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Role By Public Id : {}",
                publicId
        );

        RoleResponse response =
                roleService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Role fetched successfully."
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Get All Roles.
     *
     * @param page page number
     * @param size page size
     * @return roles
     */
    @Operation(summary = "Get All Roles")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'ROLE_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("REST Request : Get All Roles");

        PageResponse<RoleResponse> response =
                roleService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                "Roles fetched successfully."
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Delete Role.
     *
     * @param publicId role public ID
     * @return success response
     */
    @Operation(summary = "Delete Role")
    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasPermission(null, 'ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Role : {}",
                publicId
        );

        roleService.delete(publicId);

        return ResponseBuilder.success(
                null,
                RoleMessages.ROLE_DELETED
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activate Role.
     *
     * @param publicId role public ID
     * @return success response
     */
    @Operation(summary = "Activate Role")
    @PatchMapping("/{publicId}/activate")
    @PreAuthorize("hasPermission(null, 'ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Role : {}",
                publicId
        );

        roleService.activate(publicId);

        return ResponseBuilder.success(
                null,
                RoleMessages.ROLE_ACTIVATED
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivate Role.
     *
     * @param publicId role public ID
     * @return success response
     */
    @Operation(summary = "Deactivate Role")
    @PatchMapping("/{publicId}/deactivate")
    @PreAuthorize("hasPermission(null, 'ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Role : {}",
                publicId
        );

        roleService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                RoleMessages.ROLE_DEACTIVATED
        );
    }
}