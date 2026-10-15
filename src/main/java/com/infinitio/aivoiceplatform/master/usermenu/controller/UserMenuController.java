package com.infinitio.aivoiceplatform.master.usermenu.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.master.usermenu.constant.UserMenuMessages;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.CreateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.request.UpdateUserMenuRequest;
import com.infinitio.aivoiceplatform.master.usermenu.dto.response.UserMenuResponse;
import com.infinitio.aivoiceplatform.master.usermenu.service.UserMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User Menu Management.
 *
 * <p>
 * Common-library RBAC permissions are applied at the
 * endpoint level. Tenant and RoleMenu restrictions are
 * enforced inside UserMenuService.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-menus")
@Tag(
        name = "User Menu",
        description = "User Menu Management APIs"
)
public class UserMenuController {

    private final UserMenuService userMenuService;


    // =========================================================
    // CREATE
    // =========================================================

    @Operation(
            summary = "Create User Menu"
    )
    @PostMapping
    @PreAuthorize(
            "hasPermission(null, 'USER_MENU_CREATE')"
    )
    public ResponseEntity<
            ApiResponse<UserMenuResponse>
            > create(
            @Valid
            @RequestBody
            CreateUserMenuRequest request) {

        UserMenuResponse response =
                userMenuService.create(
                        request
                );

        return ResponseBuilder.created(
                response,
                UserMenuMessages.CREATED
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Operation(
            summary = "Update User Menu"
    )
    @PutMapping
    @PreAuthorize(
            "hasPermission(null, 'USER_MENU_UPDATE')"
    )
    public ResponseEntity<
            ApiResponse<UserMenuResponse>
            > update(
            @Valid
            @RequestBody
            UpdateUserMenuRequest request) {

        UserMenuResponse response =
                userMenuService.update(
                        request
                );

        return ResponseBuilder.success(
                response,
                UserMenuMessages.UPDATED
        );
    }


    // =========================================================
    // GET
    // =========================================================

    @Operation(
            summary = "Get User Menu By Public Id"
    )
    @GetMapping("/{publicId}")
    @PreAuthorize(
            "hasPermission(null, 'USER_MENU_VIEW')"
    )
    public ResponseEntity<
            ApiResponse<UserMenuResponse>
            > getByPublicId(
            @PathVariable
            String publicId) {

        UserMenuResponse response =
                userMenuService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                UserMenuMessages.FETCHED
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Operation(
            summary = "Get All User Menus"
    )
    @GetMapping
    @PreAuthorize(
            "hasPermission(null, 'USER_MENU_VIEW')"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<UserMenuResponse>>
            > getAll(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size) {

        PageResponse<UserMenuResponse> response =
                userMenuService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                UserMenuMessages.FETCHED_ALL
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Operation(
            summary = "Delete User Menu"
    )
    @DeleteMapping("/{publicId}")
    @PreAuthorize(
            "hasPermission(null, 'USER_MENU_DELETE')"
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable
            String publicId) {

        userMenuService.delete(
                publicId
        );

        return ResponseBuilder.success(
                null,
                UserMenuMessages.DELETED
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Operation(
            summary = "Activate User Menu"
    )
    @PatchMapping("/{publicId}/activate")
    @PreAuthorize(
            "hasPermission(null, 'USER_MENU_UPDATE')"
    )
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable
            String publicId) {

        userMenuService.activate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                UserMenuMessages.ACTIVATED
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Operation(
            summary = "Deactivate User Menu"
    )
    @PatchMapping("/{publicId}/deactivate")
    @PreAuthorize(
            "hasPermission(null, 'USER_MENU_UPDATE')"
    )
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable
            String publicId) {

        userMenuService.deactivate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                UserMenuMessages.DEACTIVATED
        );
    }
}