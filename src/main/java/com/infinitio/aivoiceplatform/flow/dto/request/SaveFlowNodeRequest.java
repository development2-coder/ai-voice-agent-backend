package com.infinitio.aivoiceplatform.flow.dto.request;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Flow node submitted by the visual Flow Builder.
 *
 * <p>
 * The public identifier is optional because a node without one
 * represents a newly created node.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveFlowNodeRequest {

    /**
     * Existing node public identifier.
     */
    private String publicId;

    /**
     * Unique node key within the Flow.
     */
    @NotBlank
    @Size(max = 100)
    private String nodeKey;

    /**
     * Node display name.
     */
    @NotBlank
    @Size(max = 150)
    private String name;

    /**
     * Node type.
     */
    @NotNull
    private FlowNodeType nodeType;

    /**
     * Node configuration JSON.
     */
    private String configuration;

    /**
     * X coordinate on the visual canvas.
     */
    private Double positionX;

    /**
     * Y coordinate on the visual canvas.
     */
    private Double positionY;
}