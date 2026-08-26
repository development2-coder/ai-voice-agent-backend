package com.infinitio.aivoiceplatform.flow.dto.response;

import com.infinitio.aivoiceplatform.flow.constant.FlowNodePortType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Describes an input or output port exposed by a Flow node.
 *
 * <p>
 * The frontend uses this metadata to render connection handles
 * on the visual Flow Builder.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowNodePortResponse {

    /**
     * Unique port identifier within the node.
     */
    private String portId;

    /**
     * Display name shown to the user.
     */
    private String displayName;

    /**
     * Port direction.
     */
    private FlowNodePortType type;

    /**
     * Data type accepted or produced by the port.
     */
    private String dataType;

    /**
     * Whether the port is required.
     */
    private Boolean required;

    /**
     * Whether multiple connections can use this port.
     */
    private Boolean multipleConnections;
}