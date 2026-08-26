package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;

/**
 * Resolves runtime configuration required to start
 * an AI Dialer Call Session.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface DialerCallRuntimeConfigurationService {

    /**
     * Resolves the tenant public ID.
     *
     * @param dialerCall dialer call
     * @return tenant public ID
     */
    String resolveTenantPublicId(
            DialerCall dialerCall
    );

    /**
     * Resolves the agent public ID.
     *
     * @param dialerCall dialer call
     * @return agent public ID
     */
    String resolveAgentPublicId(
            DialerCall dialerCall
    );

    /**
     * Resolves the agent version.
     *
     * @param dialerCall dialer call
     * @return agent version
     */
    Integer resolveAgentVersion(
            DialerCall dialerCall
    );

    /**
     * Resolves the initial flow node.
     *
     * @param dialerCall dialer call
     * @return flow node ID
     */
    String resolveFlowNodeId(
            DialerCall dialerCall
    );

    /**
     * Resolves the session language.
     *
     * @param dialerCall dialer call
     * @return language
     */
    String resolveLanguage(
            DialerCall dialerCall
    );
}