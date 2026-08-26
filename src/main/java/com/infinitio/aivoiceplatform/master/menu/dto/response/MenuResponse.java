package com.infinitio.aivoiceplatform.master.menu.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private String publicId;

    private String modulePublicId;

    private String parentMenuPublicId;

    private String menuCode;

    private String menuName;

    private String route;

    private String icon;

    private Integer displayOrder;

    private Integer isSystem;

    private Integer isActive;
}