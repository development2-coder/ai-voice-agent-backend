package com.infinitio.aivoiceplatform.master.rolemenu.controller;

import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.master.rolemenu.constant.RoleMenuMessages;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.CreateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.request.UpdateRoleMenuRequest;
import com.infinitio.aivoiceplatform.master.rolemenu.dto.response.RoleMenuResponse;
import com.infinitio.aivoiceplatform.master.rolemenu.service.RoleMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Role Menu Management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/role-menus")
@Tag(
        name = "Role Menu",
        description = "Role Menu Management APIs"
)
public class RoleMenuController {

    private final RoleMenuService roleMenuService;


    // =========================================================
    // CREATE
    // =========================================================

    @Operation(
            summary = "Create Role Menu"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RoleMenuResponse>> create(
            @Valid
            @RequestBody
            CreateRoleMenuRequest request) {

        log.info(
                "REST Request : Create Role Menu | rolePublicId={} | menuPublicId={}",
                request.getRolePublicId(),
                request.getMenuPublicId()
        );

        RoleMenuResponse response =
                roleMenuService.create(
                        request
                );

        return ResponseBuilder.created(
                response,
                RoleMenuMessages.CREATED
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Operation(
            summary = "Update Role Menu"
    )
    @PutMapping
    public ResponseEntity<ApiResponse<RoleMenuResponse>> update(
            @Valid
            @RequestBody
            UpdateRoleMenuRequest request) {

        log.info(
                "REST Request : Update Role Menu | publicId={}",
                request.getPublicId()
        );

        RoleMenuResponse response =
                roleMenuService.update(
                        request
                );

        return ResponseBuilder.success(
                response,
                RoleMenuMessages.UPDATED
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Operation(
            summary = "Get Role Menu By Public Id"
    )
    @GetMapping("/{publicId}")
    public ResponseEntity<ApiResponse<RoleMenuResponse>> getByPublicId(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Get Role Menu | publicId={}",
                publicId
        );

        RoleMenuResponse response =
                roleMenuService.getByPublicId(
                        publicId
                );

        return ResponseBuilder.success(
                response,
                RoleMenuMessages.FETCHED
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Operation(
            summary = "Get All Role Menus"
    )
    @GetMapping
    public ResponseEntity<
            ApiResponse<PageResponse<RoleMenuResponse>>
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
                "REST Request : Get All Role Menus | page={} | size={}",
                page,
                size
        );

        PageResponse<RoleMenuResponse> response =
                roleMenuService.getAll(
                        page,
                        size
                );

        return ResponseBuilder.success(
                response,
                RoleMenuMessages.FETCHED_ALL
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Operation(
            summary = "Delete Role Menu"
    )
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Delete Role Menu | publicId={}",
                publicId
        );

        roleMenuService.delete(
                publicId
        );

        return ResponseBuilder.success(
                null,
                RoleMenuMessages.DELETED
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Operation(
            summary = "Activate Role Menu"
    )
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Activate Role Menu | publicId={}",
                publicId
        );

        roleMenuService.activate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                RoleMenuMessages.ACTIVATED
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Operation(
            summary = "Deactivate Role Menu"
    )
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable
            String publicId) {

        log.info(
                "REST Request : Deactivate Role Menu | publicId={}",
                publicId
        );

        roleMenuService.deactivate(
                publicId
        );

        return ResponseBuilder.success(
                null,
                RoleMenuMessages.DEACTIVATED
        );
    }
}