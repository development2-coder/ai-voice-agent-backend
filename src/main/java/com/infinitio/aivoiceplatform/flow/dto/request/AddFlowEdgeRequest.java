package com.infinitio.aivoiceplatform.flow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request used to create a connection between two Flow nodes.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFlowEdgeRequest {

    /**
     * Flow public identifier.
     */
    @NotBlank
    private String flowPublicId;

    /**
     * Source node key.
     */
    @NotBlank
    @Size(max = 100)
    private String sourceNodeKey;

    /**
     * Output port on the source node.
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
     * Input port on the target node.
     */
    @NotBlank
    @Size(max = 100)
    private String targetPort;

    /**
     * Optional visual label for the connection.
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