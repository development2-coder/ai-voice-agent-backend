package com.infinitio.aivoiceplatform.master.rolepermission.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request for saving permission assignments for a role.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveRolePermissionsRequest {

    @NotNull(message = "Permission list is required.")
    private List<@NotBlank(message = "Permission public ID cannot be blank.")
            String> permissionPublicIds;
}