package com.infinitio.aivoiceplatform.flow.validator;

import com.infinitio.aivoiceplatform.flow.entity.Flow;

/**
 * Validates the structural integrity of a Flow graph.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface FlowGraphValidator {

    /**
     * Validates a Flow before it is activated.
     *
     * @param flow Flow to validate
     */
    void validateForActivation(
            Flow flow
    );
}