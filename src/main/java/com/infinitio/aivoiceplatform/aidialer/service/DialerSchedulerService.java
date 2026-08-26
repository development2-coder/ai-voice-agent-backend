package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerCallResponse;

import java.util.List;

public interface DialerSchedulerService {

    List<DialerCallResponse> processDialer(
            String dialerPublicId
    );

    void processRunningDialers();

    void stopDialer(
            String dialerPublicId
    );
}