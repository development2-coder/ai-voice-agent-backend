package com.infinitio.aivoiceplatform.master.role.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Create Role Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
    @Min(value = 1, message = "Display order must be greater than 0.")
    private Integer displayOrder;

    /**
     * System Role.
     */
    @NotNull(message = "System role flag is required.")
    @Min(0)
    @Max(1)
    private Integer isSystem;

    /**
     * Default Role.
     */
    @NotNull(message = "Default role flag is required.")
    @Min(0)
    @Max(1)
    private Integer isDefault;

}