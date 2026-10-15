package com.infinitio.aivoiceplatform.master.rolepermission.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.master.rolepermission.constant.RolePermissionMessages;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.CreateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.UpdateRolePermissionRequest;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.response.RolePermissionResponse;
import com.infinitio.aivoiceplatform.master.rolepermission.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.infinitio.aivoiceplatform.master.rolepermission.dto.request.SaveRolePermissionsRequest;
import java.util.List;

/**
 * REST Controller for Role Permission Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/role-permissions")
@Tag(
        name = "Role Permission",
        description = "Role Permission Management APIs"
)
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;


    // =========================================================
    // CREATE
    // =========================================================

    @Operation(
            summary = "Create Role Permission"
    )
    @PostMapping
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_CREATE')")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> create(
            @Valid
            @RequestBody
            CreateRolePermissionRequest request) {

        log.info(
                "REST Request : Create Role Permission"
        );

        RolePermissionResponse response =
                rolePermissionService.create(
                        request
                );

        return ResponseBuilder.created(
                response,
                RolePermissionMessages.CREATED
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Operation(
            summary = "Update Role Permission"
    )
    @PutMapping
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_UPDATE')")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> update(
            @Valid
            @RequestBody
            UpdateRolePermissionRequest request) {

        log.info(
                "REST Request : Update Role Permission | publicId={}",
                request.getPublicId()
        );

        RolePermissionResponse response =
                rolePermissionService.update(
                        request
                );

        return ResponseBuilder.success(
                response,
                RolePermissionMessages.UPDATED
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Operation(
            summary = "Get Role Permission By Public Id"
    )
    @GetMapping("/{publicId}")
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_VIEW')")
    public ResponseEntity<
            ApiResponse<RolePermissionResponse>
            > getByPublicId(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Get Role Permission | publicId={}",
                publicId
        );

        RolePermissionResponse response =
                rolePermissionService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                RolePermissionMessages.FETCHED
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Operation(
            summary = "Get All Role Permissions"
    )
    @GetMapping
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_VIEW')")
    public ResponseEntity<
            ApiResponse<List<RolePermissionResponse>>
            > getAll() {

        log.info("REST Request : Get All Role Permissions");

        List<RolePermissionResponse> response =
                rolePermissionService.getAll();

        return ResponseBuilder.success(
                response,
                RolePermissionMessages.FETCHED_ALL
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Operation(
            summary = "Delete Role Permission"
    )
    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Delete Role Permission | publicId={}",
                publicId
        );

        rolePermissionService.delete(
                publicId
        );

        return ResponseBuilder.success(
                null,
                RolePermissionMessages.DELETED
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Operation(
            summary = "Activate Role Permission"
    )
    @PatchMapping("/{publicId}/activate")
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Activate Role Permission | publicId={}",
                publicId
        );

        rolePermissionService.activate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                RolePermissionMessages.ACTIVATED
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Operation(
            summary = "Deactivate Role Permission"
    )
    @PatchMapping("/{publicId}/deactivate")
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Deactivate Role Permission | publicId={}",
                publicId
        );

        rolePermissionService.deactivate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                RolePermissionMessages.DEACTIVATED
        );
    }

    @Operation(
            summary = "Get Permissions Assigned to a Role"
    )
    @GetMapping("/role/{rolePublicId}")
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_VIEW')")
    public ResponseEntity<
            ApiResponse<List<RolePermissionResponse>>
            > getByRolePublicId(
            @PathVariable String rolePublicId) {

        log.info(
                "REST Request : Get Permissions Assigned to Role | rolePublicId={}",
                rolePublicId
        );

        List<RolePermissionResponse> response =
                rolePermissionService.getByRolePublicId(
                        rolePublicId
                );

        return ResponseBuilder.success(
                response,
                "Role permissions fetched successfully."
        );
    }

    @Operation(
            summary = "Save Permissions Assigned to a Role"
    )
    @PutMapping("/role/{rolePublicId}")
    @PreAuthorize("hasPermission(null, 'ROLE_PERMISSION_UPDATE')")
    public ResponseEntity<
            ApiResponse<List<RolePermissionResponse>>
            > saveForRole(
            @PathVariable String rolePublicId,
            @Valid @RequestBody SaveRolePermissionsRequest request) {

        log.info(
                "REST Request : Save Role Permissions | rolePublicId={}",
                rolePublicId
        );

        List<RolePermissionResponse> response =
                rolePermissionService.saveForRole(
                        rolePublicId,
                        request
                );

        return ResponseBuilder.success(
                response,
                "Role permissions saved successfully."
        );
    }


}