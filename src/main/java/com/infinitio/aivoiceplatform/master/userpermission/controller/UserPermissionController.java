package com.infinitio.aivoiceplatform.master.userpermission.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.master.userpermission.dto.request.AssignUserPermissionsRequest;
import com.infinitio.aivoiceplatform.master.userpermission.dto.response.UserPermissionResponse;
import com.infinitio.aivoiceplatform.master.userpermission.service.UserPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for individual user permissions.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userPublicId}/permissions")
@Tag(
        name = "User Permissions",
        description = "Assign and retrieve permissions for individual users"
)
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    /**
     * Replaces the permissions assigned to a user.
     */
    @Operation(summary = "Assign permissions to a user")
    @PutMapping
    public ResponseEntity<ApiResponse<List<UserPermissionResponse>>> assign(
            @PathVariable String userPublicId,
            @Valid @RequestBody AssignUserPermissionsRequest request) {

        log.info(
                "REST request to assign individual permissions. userPublicId={}",
                userPublicId
        );

        List<UserPermissionResponse> response =
                userPermissionService.assignPermissions(
                        userPublicId,
                        request.permissionPublicIds()
                );

        return ResponseBuilder.success(
                response,
                "User permissions updated successfully."
        );
    }

    /**
     * Returns permissions assigned directly to a user.
     */
    @Operation(summary = "Get permissions assigned to a user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserPermissionResponse>>> get(
            @PathVariable String userPublicId) {

        log.info(
                "REST request to get individual permissions. userPublicId={}",
                userPublicId
        );

        List<UserPermissionResponse> response =
                userPermissionService.getUserPermissions(userPublicId);

        return ResponseBuilder.success(
                response,
                "User permissions fetched successfully."
        );
    }
}