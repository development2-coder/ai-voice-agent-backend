package com.infinitio.aivoiceplatform.master.rolemenu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Role Menu Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuResponse {

    private String publicId;

    private String rolePublicId;

    private String roleCode;

    private String roleName;

    private String menuPublicId;

    private String menuCode;

    private String menuName;

    private String route;

    private Integer isVisible;

    private Integer isActive;
}