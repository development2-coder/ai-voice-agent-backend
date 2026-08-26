package com.infinitio.aivoiceplatform.master.menu.dto.request;

import com.infinitio.aivoiceplatform.master.menu.constant.MenuConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {

    @NotBlank(message = "Module is required.")
    private String modulePublicId;

    private String parentMenuPublicId;

    @NotBlank(message = "Menu code is required.")
    @Size(max = MenuConstants.MENU_CODE_MAX_LENGTH)
    private String menuCode;

    @NotBlank(message = "Menu name is required.")
    @Size(max = MenuConstants.MENU_NAME_MAX_LENGTH)
    private String menuName;

    @Size(max = MenuConstants.ROUTE_MAX_LENGTH)
    private String route;

    @Size(max = MenuConstants.ICON_MAX_LENGTH)
    private String icon;

    private Integer displayOrder;

    private Integer isSystem;
}