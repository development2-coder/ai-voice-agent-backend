package com.infinitio.aivoiceplatform.flow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request used to update an existing Flow edge.
 *
 * <p>
 * The edge public identifier identifies the existing connection,
 * while the remaining fields define its current source, target,
 * port and execution configuration.
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
public class UpdateFlowEdgeRequest {

    /**
     * Edge public identifier.
     */
    @NotBlank
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
     * Optional visual label.
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
    @NotNull
    private Integer priority;
}