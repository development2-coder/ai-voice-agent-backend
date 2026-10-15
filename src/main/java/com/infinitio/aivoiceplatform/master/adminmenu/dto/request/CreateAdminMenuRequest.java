package com.infinitio.aivoiceplatform.master.adminmenu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request for assigning a Menu to an ADMIN.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminMenuRequest {

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
     *
     * 1 = visible
     * 0 = hidden
     */
    private Integer isVisible;
}