package com.infinitio.aivoiceplatform.master.menu.controller;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.master.menu.dto.request.CreateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.request.UpdateMenuRequest;
import com.infinitio.aivoiceplatform.master.menu.dto.response.MenuResponse;
import com.infinitio.aivoiceplatform.master.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Menu Controller.
 *
 * Provides REST APIs for managing application menus.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Create a new Menu.
     *
     * POST /api/v1/menus
     *
     * @param request create menu request
     * @return created menu
     */
    @PostMapping
    public ResponseEntity<MenuResponse> create(
            @Valid @RequestBody CreateMenuRequest request) {

        log.info(
                "REST Request : Create Menu | menuCode={}",
                request.getMenuCode()
        );

        MenuResponse response =
                menuService.create(
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
     * Update an existing Menu.
     *
     * PUT /api/v1/menus
     *
     * @param request update menu request
     * @return updated menu
     */
    @PutMapping
    public ResponseEntity<MenuResponse> update(
            @Valid @RequestBody UpdateMenuRequest request) {

        log.info(
                "REST Request : Update Menu | publicId={}",
                request.getPublicId()
        );

        MenuResponse response =
                menuService.update(
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
     * Get Menu by public ID.
     *
     * GET /api/v1/menus/{publicId}
     *
     * @param publicId menu public ID
     * @return menu
     */
    @GetMapping("/{publicId}")
    public ResponseEntity<MenuResponse> getByPublicId(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Get Menu | publicId={}",
                publicId
        );

        MenuResponse response =
                menuService.getByPublicId(
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
     * Get all non-deleted Menus with pagination.
     *
     * GET /api/v1/menus?page=0&size=10
     *
     * @param page page number
     * @param size page size
     * @return paginated menu response
     */
    @GetMapping
    public ResponseEntity<PageResponse<MenuResponse>> getAll(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size) {

        log.info(
                "REST Request : Get All Menus | page={} | size={}",
                page,
                size
        );

        PageResponse<MenuResponse> response =
                menuService.getAll(
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
     * Soft delete Menu.
     *
     * DELETE /api/v1/menus/{publicId}
     *
     * @param publicId menu public ID
     * @return no content
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Delete Menu | publicId={}",
                publicId
        );

        menuService.delete(
                publicId
        );

        return ResponseEntity.noContent()
                .build();
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activate Menu.
     *
     * PATCH /api/v1/menus/{publicId}/activate
     *
     * @param publicId menu public ID
     * @return no content
     */
    @PatchMapping("/{publicId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Activate Menu | publicId={}",
                publicId
        );

        menuService.activate(
                publicId
        );

        return ResponseEntity.noContent()
                .build();
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivate Menu.
     *
     * PATCH /api/v1/menus/{publicId}/deactivate
     *
     * @param publicId menu public ID
     * @return no content
     */
    @PatchMapping("/{publicId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable String publicId) {

        log.info(
                "REST Request : Deactivate Menu | publicId={}",
                publicId
        );

        menuService.deactivate(
                publicId
        );

        return ResponseEntity.noContent()
                .build();
    }
}