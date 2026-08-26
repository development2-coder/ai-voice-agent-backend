package com.infinitio.aivoiceplatform.runtime.service;

import com.infinitio.aivoiceplatform.runtime.context.RuntimeContext;

/**
 * Provides serialization and deserialization operations for
 * call runtime context.
 *
 * <p>
 * Runtime context is persisted as JSON in the existing
 * flow execution context data rather than in a separate
 * runtime-state table.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface RuntimeContextService {

    /**
     * Converts runtime context into its JSON representation.
     *
     * @param context runtime context
     * @return serialized runtime context
     */
    String serialize(
            RuntimeContext context
    );

    /**
     * Converts persisted JSON into runtime context.
     *
     * @param contextData persisted context data
     * @return runtime context
     */
    RuntimeContext deserialize(
            String contextData
    );
}