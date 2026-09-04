package com.infinitio.aivoiceplatform.agent.dto.response;

import com.infinitio.aivoiceplatform.flow.dto.response.FlowDefinitionResponse;
import com.infinitio.aivoiceplatform.flow.dto.response.FlowNodeDefinitionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents the complete Agent workspace required by
 * the visual Flow Builder.
 *
 * <p>
 * The workspace combines Agent information, the latest Flow
 * definition and the available Flow node library.
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
public class AgentWorkspaceResponse {

    /**
     * Agent details.
     */
    private AgentResponse agent;

    /**
     * Complete Flow definition.
     */
    private FlowDefinitionResponse flow;

    /**
     * Available node definitions for the visual builder.
     */
    private List<FlowNodeDefinitionResponse> nodeTypes;
}