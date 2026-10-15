package com.infinitio.aivoiceplatform.telephony.inbound.service;

import com.infinitio.aivoiceplatform.call.entity.Call;

/**
 * Handles lifecycle status updates for inbound calls.
 *
 * <p>
 * This service keeps inbound call status changes separate from
 * WebSocket transport handling.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface InboundCallStatusService {

    /**
     * Marks an inbound call as in progress.
     *
     * @param call call being processed
     * @return updated call
     */
    Call markInProgress(Call call);

    /**
     * Marks an inbound call as completed.
     *
     * @param call call that has ended normally
     * @return updated call
     */
    Call markCompleted(Call call);

    /**
     * Marks an inbound call as failed.
     *
     * @param call call that failed
     * @return updated call
     */
    Call markFailed(Call call);
}