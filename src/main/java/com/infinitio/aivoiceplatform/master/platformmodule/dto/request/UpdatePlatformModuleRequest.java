package com.infinitio.aivoiceplatform.master.platformmodule.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Request DTO for updating a Platform Module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlatformModuleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Public identifier of the module.
     */
    @NotBlank(message = "Public Id is required.")
    @Size(max = 36, message = "Public Id cannot exceed 36 characters.")
    private String publicId;

    /**
     * Unique module code.
     */
    @NotBlank(message = "Module code is required.")
    @Size(max = 50, message = "Module code cannot exceed 50 characters.")
    private String moduleCode;

    /**
     * Module name.
     */
    @NotBlank(message = "Module name is required.")
    @Size(max = 150, message = "Module name cannot exceed 150 characters.")
    private String moduleName;

    /**
     * Display name shown on UI.
     */
    @NotBlank(message = "Display name is required.")
    @Size(max = 150, message = "Display name cannot exceed 150 characters.")
    private String displayName;

    /**
     * Module description.
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description;

    /**
     * Module icon.
     */
    @Size(max = 100, message = "Icon cannot exceed 100 characters.")
    private String icon;

    /**
     * Frontend route.
     */
    @Size(max = 255, message = "Route cannot exceed 255 characters.")
    private String route;

    /**
     * Display order in sidebar/menu.
     */
    @NotNull(message = "Display order is required.")
    @Min(value = 1, message = "Display order must be greater than zero.")
    @Max(value = 9999, message = "Display order cannot exceed 9999.")
    private Integer displayOrder;

    /**
     * Indicates whether this is a system module.
     * 1 = Yes
     * 0 = No
     */
    @NotNull(message = "System module flag is required.")
    @Min(value = 0, message = "System module must be either 0 or 1.")
    @Max(value = 1, message = "System module must be either 0 or 1.")
    private Integer isSystem;

    /**
     * Indicates whether this module is visible.
     * 1 = Yes
     * 0 = No
     */
    @NotNull(message = "Visible flag is required.")
    @Min(value = 0, message = "Visible flag must be either 0 or 1.")
    @Max(value = 1, message = "Visible flag must be either 0 or 1.")
    private Integer isVisible;

    /**
     * Module status.
     * 1 = Active
     * 0 = Inactive
     */
    @NotNull(message = "Active status is required.")
    @Min(value = 0, message = "Active status must be either 0 or 1.")
    @Max(value = 1, message = "Active status must be either 0 or 1.")
    private Integer isActive;

}