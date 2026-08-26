package com.infinitio.aivoiceplatform.phonenumber.validator;

import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import com.infinitio.aivoiceplatform.phonenumber.constant.PhoneNumberMessages;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.CreatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.UpdatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import com.infinitio.aivoiceplatform.phonenumber.repository.PhoneNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for Phone Number.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class PhoneNumberValidator {

    private final PhoneNumberRepository phoneNumberRepository;

    public void validateForCreate(
            CreatePhoneNumberRequest request) {

        if (phoneNumberRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {

            throw new ConflictException(
                    PhoneNumberMessages.NUMBER_ALREADY_EXISTS
            );
        }

        if (request.getProviderNumberId() != null
                && !request.getProviderNumberId().isBlank()
                && phoneNumberRepository.existsByProviderNumberId(
                request.getProviderNumberId())) {

            throw new ConflictException(
                    PhoneNumberMessages
                            .PROVIDER_NUMBER_ID_ALREADY_EXISTS
            );
        }
    }

    public void validateForUpdate(
            UpdatePhoneNumberRequest request) {

        PhoneNumber existing =
                validateAndGet(request.getPublicId());

        if (!existing.getPhoneNumber()
                .equals(request.getPhoneNumber())
                && phoneNumberRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {

            throw new ConflictException(
                    PhoneNumberMessages.NUMBER_ALREADY_EXISTS
            );
        }

        String existingProviderNumberId =
                existing.getProviderNumberId();

        String requestProviderNumberId =
                request.getProviderNumberId();

        if (requestProviderNumberId != null
                && !requestProviderNumberId.isBlank()
                && !requestProviderNumberId.equals(
                existingProviderNumberId)
                && phoneNumberRepository
                .existsByProviderNumberId(
                        requestProviderNumberId)) {

            throw new ConflictException(
                    PhoneNumberMessages
                            .PROVIDER_NUMBER_ID_ALREADY_EXISTS
            );
        }
    }

    public PhoneNumber validateAndGet(
            String publicId) {

        return phoneNumberRepository
                .findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PhoneNumberMessages.NOT_FOUND
                        )
                );
    }
}