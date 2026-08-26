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
import org.springframework.web.bind.annotation.*;

/**
 * Role Controller.
 *
 * Manages Role APIs.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
@Tag(name = "Role", description = "Role Management APIs")
public class RoleController {

    private final RoleService roleService;

    /**
     * Create Role.
     */
    @Operation(summary = "Create Role")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(
            @Valid @RequestBody CreateRoleRequest request) {

        log.info("REST Request : Create Role");

        RoleResponse response = roleService.create(request);

        return ResponseBuilder.created(
                response,
                RoleMessages.ROLE_CREATED
        );

    }

    /**
     * Update Role.
     */
    @Operation(summary = "Update Role")
    @PutMapping
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("REST Request : Update Role");

        RoleResponse response = roleService.update(request);

        return ResponseBuilder.success(
                response,
                RoleMessages.ROLE_UPDATED
        );

    }

    /**
     * Get Role By Public Id.
     */
    @Operation(summary = "Get Role By Public Id")
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<RoleResponse>> getByPublicId(
            @PathVariable String publicId) {

        log.info("REST Request : Get Role By Public Id : {}", publicId);

        RoleResponse response = roleService.getByPublicId(publicId);

        return ResponseBuilder.success(
                response,
                "Role fetched successfully."
        );

    }

    /**
     * Get All Roles.
     */
    @Operation(summary = "Get All Roles")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getAll(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("REST Request : Get All Roles");

        PageResponse<RoleResponse> response =
                roleService.getAll(page, size);

        return ResponseBuilder.success(
                response,
                "Roles fetched successfully."
        );

    }

    /**
     * Delete Role.
     */
    @Operation(summary = "Delete Role")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        log.info("REST Request : Delete Role : {}", publicId);

        roleService.delete(publicId);

        return ResponseBuilder.success(
                null,
                RoleMessages.ROLE_DELETED
        );

    }

    /**
     * Activate Role.
     */
    @Operation(summary = "Activate Role")
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        log.info("REST Request : Activate Role : {}", publicId);

        roleService.activate(publicId);

        return ResponseBuilder.success(
                null,
                RoleMessages.ROLE_ACTIVATED
        );

    }

    /**
     * Deactivate Role.
     */
    @Operation(summary = "Deactivate Role")
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        log.info("REST Request : Deactivate Role : {}", publicId);

        roleService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                RoleMessages.ROLE_DEACTIVATED
        );

    }

}