package com.infinitio.aivoiceplatform.runtime.context;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the runtime context associated with an active call.
 *
 * <p>
 * The runtime context is persisted inside the existing
 * flow execution context data instead of using a separate
 * runtime-state table.
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
public class RuntimeContext {

    /**
     * Public identifier of the tenant associated with the call.
     */
    private String tenantId;

    /**
     * Public identifier of the agent handling the call.
     */
    private String agentId;

    /**
     * Version of the agent used for the call.
     */
    private Integer agentVersion;

    /**
     * Language selected for the call.
     */
    private String language;

    /**
     * Current conversation turn.
     */
    private Integer turnIndex;

    /**
     * Values collected during the conversation.
     */
    @Builder.Default
    private Map<String, String> collectedSlots =
            new HashMap<>();
}