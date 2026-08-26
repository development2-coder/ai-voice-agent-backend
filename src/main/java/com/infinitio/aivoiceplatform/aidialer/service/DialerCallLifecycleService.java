package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;

public interface DialerCallLifecycleService {

    void updateStatus(
            DialerCall call,
            CallAttemptStatus status
    );

    void markAnswered(
            DialerCall call
    );

    void completeCall(
            DialerCall call,
            Integer durationSeconds,
            String hangupReason
    );

    void failCall(
            DialerCall call,
            String failureReason
    );
}