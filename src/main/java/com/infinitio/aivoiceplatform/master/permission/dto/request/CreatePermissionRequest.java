package com.infinitio.aivoiceplatform.master.permission.dto.request;

import com.infinitio.aivoiceplatform.master.permission.constant.PermissionConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {

    @NotBlank(message = "Permission code is required.")
    @Size(max = PermissionConstants.PERMISSION_CODE_MAX_LENGTH)
    private String permissionCode;

    @NotBlank(message = "Permission name is required.")
    @Size(max = PermissionConstants.PERMISSION_NAME_MAX_LENGTH)
    private String permissionName;

    @Size(max = PermissionConstants.DESCRIPTION_MAX_LENGTH)
    private String description;

    private Integer displayOrder;

    private Integer isSystem;
}