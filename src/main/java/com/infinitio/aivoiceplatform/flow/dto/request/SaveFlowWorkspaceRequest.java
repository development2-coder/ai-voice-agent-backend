package com.infinitio.aivoiceplatform.flow.dto.request;

import com.infinitio.aivoiceplatform.flow.constant.FlowType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request used to save the complete visual Flow Builder workspace.
 *
 * <p>
 * The request represents the complete state of the Flow Builder
 * canvas including Flow metadata, nodes and connections.
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
public class SaveFlowWorkspaceRequest {

    /**
     * Flow name.
     */
    @NotBlank
    private String name;

    /**
     * Flow description.
     */
    private String description;

    /**
     * Flow type.
     */
    @NotNull
    private FlowType flowType;

    /**
     * Nodes currently present on the canvas.
     */
    @Valid
    @NotEmpty
    private List<SaveFlowNodeRequest> nodes;

    /**
     * Edges currently present on the canvas.
     */
    @Valid
    private List<SaveFlowEdgeRequest> edges;
}