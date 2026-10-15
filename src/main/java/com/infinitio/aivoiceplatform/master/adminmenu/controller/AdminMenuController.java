package com.infinitio.aivoiceplatform.master.adminmenu.controller;

import com.infi.library.annotation.RequiresPermission;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import com.infinitio.aivoiceplatform.master.adminmenu.constant.AdminMenuMessages;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.CreateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.request.UpdateAdminMenuRequest;
import com.infinitio.aivoiceplatform.master.adminmenu.dto.response.AdminMenuResponse;
import com.infinitio.aivoiceplatform.master.adminmenu.service.AdminMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ADMIN menu management controller.
 *
 * <p>
 * ADMIN menu assignments are managed by SUPER_ADMIN.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin-menus")
public class AdminMenuController {

    private final AdminMenuService adminMenuService;


    @PostMapping
    @RequiresPermission("ADMIN_MENU_CREATE")
    public ResponseEntity<ApiResponse<AdminMenuResponse>> create(
            @Valid @RequestBody
            CreateAdminMenuRequest request) {

        return ResponseBuilder.created(
                adminMenuService.create(request),
                AdminMenuMessages.CREATED
        );
    }


    @PutMapping
    @RequiresPermission("ADMIN_MENU_UPDATE")
    public ResponseEntity<ApiResponse<AdminMenuResponse>> update(
            @Valid @RequestBody
            UpdateAdminMenuRequest request) {

        return ResponseBuilder.success(
                adminMenuService.update(request),
                AdminMenuMessages.UPDATED
        );
    }


    @GetMapping("/{publicId}")
    @RequiresPermission("ADMIN_MENU_VIEW")
    public ResponseEntity<ApiResponse<AdminMenuResponse>>
    getByPublicId(
            @PathVariable String publicId) {

        return ResponseBuilder.success(
                adminMenuService.getByPublicId(publicId),
                AdminMenuMessages.FETCHED
        );
    }


    @GetMapping
    @RequiresPermission("ADMIN_MENU_VIEW")
    public ResponseEntity<
            ApiResponse<PageResponse<AdminMenuResponse>>
            > getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseBuilder.success(
                adminMenuService.getAll(page, size),
                AdminMenuMessages.FETCHED_ALL
        );
    }


    @DeleteMapping("/{publicId}")
    @RequiresPermission("ADMIN_MENU_DELETE")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String publicId) {

        adminMenuService.delete(publicId);

        return ResponseBuilder.success(
                null,
                AdminMenuMessages.DELETED
        );
    }


    @PatchMapping("/{publicId}/activate")
    @RequiresPermission("ADMIN_MENU_UPDATE")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable String publicId) {

        adminMenuService.activate(publicId);

        return ResponseBuilder.success(
                null,
                AdminMenuMessages.ACTIVATED
        );
    }


    @PatchMapping("/{publicId}/deactivate")
    @RequiresPermission("ADMIN_MENU_UPDATE")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable String publicId) {

        adminMenuService.deactivate(publicId);

        return ResponseBuilder.success(
                null,
                AdminMenuMessages.DEACTIVATED
        );
    }
}