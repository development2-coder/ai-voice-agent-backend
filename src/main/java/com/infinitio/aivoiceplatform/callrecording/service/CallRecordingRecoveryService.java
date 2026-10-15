package com.infinitio.aivoiceplatform.callrecording.service;

import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;

/**
 * Service responsible for recovering call recordings
 * when the normal telephony callback is not received.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallRecordingRecoveryService {

    /**
     * Recovers a recording using the provider call ID.
     *
     * <p>
     * The service retrieves the latest call details from
     * the telephony provider and stores the recording when
     * a RecordingUrl is available.
     * </p>
     *
     * @param callPublicId platform call public identifier
     * @param providerCallId provider call identifier
     */
    void recoverRecording(
            String callPublicId,
            String providerCallId
    );
}