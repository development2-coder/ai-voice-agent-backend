package com.infinitio.aivoiceplatform.master.permission.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {

    private String publicId;

    private String permissionCode;

    private String permissionName;

    private String description;

    private Integer displayOrder;

    private Integer isSystem;

    private Integer isActive;
}