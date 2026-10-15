package com.infinitio.aivoiceplatform.master.adminmenu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin Menu response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMenuResponse {

    private String publicId;

    private String adminPublicId;

    private String adminName;

    private String menuPublicId;

    private String menuCode;

    private String menuName;

    private String route;

    private String icon;

    private Integer displayOrder;

    private Integer isVisible;

    private Integer isActive;
}