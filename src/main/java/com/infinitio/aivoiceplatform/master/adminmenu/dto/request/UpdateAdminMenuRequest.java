package com.infinitio.aivoiceplatform.master.adminmenu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for updating an ADMIN menu assignment.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminMenuRequest {

    /**
     * AdminMenu public ID.
     */
    @NotBlank(
            message = "Public ID is required."
    )
    private String publicId;

    /**
     * ADMIN/client public ID.
     */
    @NotBlank(
            message = "Admin public ID is required."
    )
    private String adminPublicId;

    /**
     * Menu public ID.
     */
    @NotBlank(
            message = "Menu public ID is required."
    )
    private String menuPublicId;

    /**
     * Menu visibility.
     */
    private Integer isVisible;
}