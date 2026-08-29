package com.infinitio.aivoiceplatform.flow.dto.response;

import lombok.*;

/**
 * Represents a Flow edge returned to the Flow Builder frontend.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowEdgeResponse {

    /**
     * Edge public identifier.
     */
    private String publicId;

    /**
     * Source node key.
     */
    private String sourceNodeKey;

    /**
     * Source output port.
     */
    private String sourcePort;

    /**
     * Target node key.
     */
    private String targetNodeKey;

    /**
     * Target input port.
     */
    private String targetPort;

    /**
     * Optional connection label.
     */
    private String label;

    /**
     * Optional condition expression.
     */
    private String conditionExpression;

    /**
     * Execution priority.
     */
    private Integer priority;
}