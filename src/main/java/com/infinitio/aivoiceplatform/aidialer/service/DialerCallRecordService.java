package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;

/**
 * Creates the platform-level Call record for an AI Dialer attempt.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface DialerCallRecordService {

    /**
     * Creates the master Call record.
     *
     * @param dialerCall dialer call attempt
     * @param fromNumber caller number
     * @return public ID of the created Call
     */
    String createCallRecord(
            DialerCall dialerCall,
            String fromNumber
    );
}