package com.infinitio.aivoiceplatform.flow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Flow connection submitted by the visual
 * Flow Builder.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveFlowEdgeRequest {

    /**
     * Existing edge public identifier.
     */
    private String publicId;

    /**
     * Source node key.
     */
    @NotBlank
    @Size(max = 100)
    private String sourceNodeKey;

    /**
     * Source output port.
     */
    @NotBlank
    @Size(max = 100)
    private String sourcePort;

    /**
     * Target node key.
     */
    @NotBlank
    @Size(max = 100)
    private String targetNodeKey;

    /**
     * Target input port.
     */
    @NotBlank
    @Size(max = 100)
    private String targetPort;

    /**
     * Optional edge label.
     */
    @Size(max = 150)
    private String label;

    /**
     * Optional condition expression.
     */
    @Size(max = 1000)
    private String conditionExpression;

    /**
     * Execution priority.
     */
    private Integer priority;
}