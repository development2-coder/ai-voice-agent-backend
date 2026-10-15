package com.infinitio.aivoiceplatform.master.usermenu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Update User Menu Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserMenuRequest {

    /**
     * User menu public ID.
     */
    @NotBlank(
            message = "Public ID is required."
    )
    private String publicId;

    /**
     * User receiving the menu access.
     */
    @NotBlank(
            message = "User is required."
    )
    private String userPublicId;

    /**
     * Menu to assign.
     */
    @NotBlank(
            message = "Menu is required."
    )
    private String menuPublicId;

    /**
     * 1 = Visible
     * 0 = Hidden.
     *
     * Existing value is retained when null.
     */
    private Integer isVisible;
}