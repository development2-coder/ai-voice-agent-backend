package com.infinitio.aivoiceplatform.master.role.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Update Role Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Role Public Id.
     */
    @NotBlank(message = "Role public id is required.")
    private String publicId;

    /**
     * Role Code.
     */
    @NotBlank(message = "Role code is required.")
    private String roleCode;

    /**
     * Role Name.
     */
    @NotBlank(message = "Role name is required.")
    private String roleName;

    /**
     * Description.
     */
    private String description;

    /**
     * Display Order.
     */
    @NotNull(message = "Display order is required.")
    @Min(1)
    private Integer displayOrder;

    /**
     * System Role.
     */
    @NotNull
    @Min(0)
    @Max(1)
    private Integer isSystem;

    /**
     * Default Role.
     */
    @NotNull
    @Min(0)
    @Max(1)
    private Integer isDefault;

}