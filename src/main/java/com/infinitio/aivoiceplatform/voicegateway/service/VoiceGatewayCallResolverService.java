package com.infinitio.aivoiceplatform.voicegateway.service;

/**
 * Resolves an application Call from a telephony provider
 * call identifier.
 *
 * <p>
 * Provider call identifiers and application call identifiers
 * are intentionally treated as separate identifiers.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface VoiceGatewayCallResolverService {

    /**
     * Resolves the application's Call public identifier from
     * the provider call identifier.
     *
     * @param providerCallId provider supplied call identifier
     * @return application Call public identifier
     */
    String resolveCallId(
            String providerCallId
    );
}