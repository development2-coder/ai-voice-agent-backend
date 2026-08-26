package com.infinitio.aivoiceplatform.master.rolemenu.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Create Role Menu Request.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleMenuRequest {

    @NotBlank(
            message = "Role is required."
    )
    private String rolePublicId;


    @NotBlank(
            message = "Menu is required."
    )
    private String menuPublicId;


    /**
     * 1 = Visible
     * 0 = Hidden
     *
     * If null, service defaults it to visible.
     */
    private Integer isVisible;
}