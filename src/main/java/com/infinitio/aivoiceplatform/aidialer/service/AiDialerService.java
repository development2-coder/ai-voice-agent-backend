package com.infinitio.aivoiceplatform.aidialer.service;

import com.infinitio.aivoiceplatform.aidialer.dto.request.CreateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.PauseDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.ResumeDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.StartDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.UpdateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerResponse;

import java.util.List;

public interface AiDialerService {

    DialerResponse create(
            CreateAiDialerRequest request
    );

    DialerResponse getByPublicId(
            String publicId
    );

    List<DialerResponse> getAll();

    List<DialerResponse> getByCampaign(
            String campaignPublicId
    );

    DialerResponse update(
            String publicId,
            UpdateAiDialerRequest request
    );

    void delete(
            String publicId
    );

    DialerResponse start(
            StartDialerRequest request
    );

    DialerResponse pause(
            PauseDialerRequest request
    );

    DialerResponse resume(
            ResumeDialerRequest request
    );

    DialerResponse stop(
            String publicId
    );
}