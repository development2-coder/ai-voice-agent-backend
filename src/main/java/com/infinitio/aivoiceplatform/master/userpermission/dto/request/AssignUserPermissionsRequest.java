package com.infinitio.aivoiceplatform.master.userpermission.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request to replace a user's individual permission assignments.
 */
public record AssignUserPermissionsRequest(

        @NotNull(message = "Permission public IDs are required.")
        @Size(max = 500, message = "Too many permissions were supplied.")
        List<
                @NotNull(message = "Permission public ID cannot be null.")
                @Size(min = 1, max = 36,
                        message = "Permission public ID must be valid.")
                        String
                > permissionPublicIds

) {
}