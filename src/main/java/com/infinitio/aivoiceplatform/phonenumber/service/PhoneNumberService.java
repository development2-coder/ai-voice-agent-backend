package com.infinitio.aivoiceplatform.phonenumber.service;

import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.CreatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.UpdatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.response.PhoneNumberResponse;

/**
 * Service interface for Phone Number.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
public interface PhoneNumberService {

    PhoneNumberResponse create(
            CreatePhoneNumberRequest request
    );

    PhoneNumberResponse update(
            UpdatePhoneNumberRequest request
    );

    PhoneNumberResponse getByPublicId(
            String publicId
    );

    PageResponse<PhoneNumberResponse> getAll(
            int page,
            int size
    );

    void delete(String publicId);

    void activate(String publicId);

    void deactivate(String publicId);
}