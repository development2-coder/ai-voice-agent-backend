package com.infinitio.aivoiceplatform.master.menu.service;

import com.infinitio.aivoiceplatform.master.menu.dto.response.MenuResponse;

import java.util.List;

/**
 * Service for resolving menus accessible to the current user.
 *
 * <p>
 * Menu management and runtime menu access are intentionally separated.
 * Menu management handles creation and configuration of menu master data,
 * while this service determines which menus the authenticated user can see.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface MenuAccessService {

    /**
     * Get menus accessible to the currently authenticated user.
     *
     * @return accessible menus
     */
    List<MenuResponse> getAccessibleMenus();
}