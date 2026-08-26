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
    public ResponseEntity<
            ApiResponse<PageResponse<RolePermissionResponse>>
            > getAll(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size) {

        log.info(
                "REST Request : Get All Role Permissions | page={} | size={}",
                page,
                size
        );

        PageResponse<RolePermissionResponse> response =
                rolePermissionService.getAll(
                        page,
                        size
                );

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
}