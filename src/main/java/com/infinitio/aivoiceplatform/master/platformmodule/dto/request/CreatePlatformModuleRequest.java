package com.infinitio.aivoiceplatform.master.platformmodule.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating a Platform Module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlatformModuleRequest {

    @NotBlank(message = "Module code is required.")
    @Size(max = 50, message = "Module code cannot exceed 50 characters.")
    private String moduleCode;

    @NotBlank(message = "Module name is required.")
    @Size(max = 150, message = "Module name cannot exceed 150 characters.")
    private String moduleName;

    @NotBlank(message = "Display name is required.")
    @Size(max = 150, message = "Display name cannot exceed 150 characters.")
    private String displayName;

    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description;

    @Size(max = 100, message = "Icon cannot exceed 100 characters.")
    private String icon;

    @Size(max = 255, message = "Route cannot exceed 255 characters.")
    private String route;

    @NotNull(message = "Display order is required.")
    @Min(value = 1, message = "Display order must be greater than zero.")
    private Integer displayOrder;

    @NotNull(message = "System module flag is required.")
    private Integer isSystem;

    @NotNull(message = "Visible flag is required.")
    private Integer isVisible;

}