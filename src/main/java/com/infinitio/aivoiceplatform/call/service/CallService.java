package com.infinitio.aivoiceplatform.call.service;

import com.infinitio.aivoiceplatform.call.dto.request.CreateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.request.UpdateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.response.CallResponse;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;

/**
 * Service interface for Call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface CallService {

    CallResponse create(
            CreateCallRequest request
    );

    CallResponse update(
            UpdateCallRequest request
    );

    CallResponse getByPublicId(
            String publicId
    );

    PageResponse<CallResponse> getAll(
            int page,
            int size
    );

    PageResponse<CallResponse> getByCampaignContact(
            String campaignContactPublicId,
            int page,
            int size
    );

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}