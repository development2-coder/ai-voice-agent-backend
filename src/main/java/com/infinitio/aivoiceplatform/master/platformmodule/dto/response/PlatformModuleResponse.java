package com.infinitio.aivoiceplatform.master.platformmodule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Response DTO for Platform Module.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformModuleResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Public Identifier.
     */
    private String publicId;

    /**
     * Module Code.
     */
    private String moduleCode;

    /**
     * Module Name.
     */
    private String moduleName;

    /**
     * Display Name.
     */
    private String displayName;

    /**
     * Description.
     */
    private String description;

    /**
     * Sidebar Icon.
     */
    private String icon;

    /**
     * Frontend Route.
     */
    private String route;

    /**
     * Display Order.
     */
    private Integer displayOrder;

    /**
     * Indicates whether this is a System Module.
     */
    private Boolean system;

    /**
     * Indicates whether this module is visible.
     */
    private Boolean visible;

    /**
     * Indicates whether this module is active.
     */
    private Boolean active;

    /**
     * Created Date & Time.
     */
    private LocalDateTime createdAt;

    /**
     * Updated Date & Time.
     */
    private LocalDateTime updatedAt;

}