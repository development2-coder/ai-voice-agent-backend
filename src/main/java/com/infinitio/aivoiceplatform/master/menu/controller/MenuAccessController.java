package com.infinitio.aivoiceplatform.master.menu.controller;

import com.infinitio.aivoiceplatform.master.menu.dto.response.MenuResponse;
import com.infinitio.aivoiceplatform.master.menu.service.MenuAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.infinitio.aivoiceplatform.common.dto.ApiResponse;
import com.infinitio.aivoiceplatform.common.util.ResponseBuilder;
import java.util.List;

/**
 * Controller for resolving menus accessible to the current user.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/my-menus")
@RequiredArgsConstructor
public class MenuAccessController {

    private final MenuAccessService menuAccessService;

    /**
     * Get menus accessible to the authenticated user.
     *
     * @return accessible menus
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMyMenus() {

        return ResponseBuilder.success(
                menuAccessService.getAccessibleMenus(),
                "Accessible menus fetched successfully."
        );
    }
}