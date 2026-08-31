package com.infinitio.aivoiceplatform.flow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO representing the complete definition of a Flow.
 *
 * <p>
 * A Flow definition contains the Flow metadata together with
 * all active nodes and edges required by the Flow Builder.
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
public class FlowDefinitionResponse {

    /**
     * Flow metadata.
     */
    private FlowResponse flow;

    /**
     * Active nodes belonging to the Flow.
     */
    private List<FlowNodeResponse> nodes;

    /**
     * Active edges belonging to the Flow.
     */
    private List<FlowEdgeResponse> edges;
}