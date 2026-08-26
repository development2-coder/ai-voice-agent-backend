package com.infinitio.aivoiceplatform.master.role.dto.response;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Role Response.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String publicId;

    private String roleCode;

    private String roleName;

    private String description;

    private Integer displayOrder;

    private Integer isSystem;

    private Integer isDefault;

    private Integer isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}