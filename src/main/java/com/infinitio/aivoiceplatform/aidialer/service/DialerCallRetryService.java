package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;

import java.util.List;

public interface DialerCallRetryService {

    boolean canRetry(
            AiDialer dialer,
            Long campaignContactId
    );

    int calculateNextAttemptNumber(
            List<DialerCall> previousCalls
    );

    boolean hasActiveAttempt(
            Long campaignContactId
    );
}