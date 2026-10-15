package com.infinitio.aivoiceplatform.master.usermenu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User Menu Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuResponse {

    private String publicId;

    private String userPublicId;

    private String username;

    private String userName;

    private String menuPublicId;

    private String menuCode;

    private String menuName;

    private String route;

    private Integer isVisible;

    private Integer isActive;
}