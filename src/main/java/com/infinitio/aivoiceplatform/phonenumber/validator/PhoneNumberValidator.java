package com.infinitio.aivoiceplatform.phonenumber.validator;

import com.infinitio.aivoiceplatform.exception.BadRequestException;
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
 * Validator for phone-number management.
 *
 * Tenant access is checked in PhoneNumberServiceImpl after resolving
 * the entity; this class validates request values and uniqueness.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class PhoneNumberValidator {

    private static final Integer NOT_DELETED = 0;

    private final PhoneNumberRepository phoneNumberRepository;

    /**
     * Validates phone-number creation.
     *
     * @param request create request
     */
    public void validateForCreate(CreatePhoneNumberRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Phone number create request is required."
            );
        }

        if (request.getPhoneNumber() == null
                || request.getPhoneNumber().isBlank()) {
            throw new BadRequestException(
                    "Phone number is required."
            );
        }

        if (request.getAgentPublicId() == null
                || request.getAgentPublicId().isBlank()) {
            throw new BadRequestException(
                    "Agent is required."
            );
        }

        if (request.getProvider() == null
                || request.getProvider().isBlank()) {
            throw new BadRequestException(
                    "Provider is required."
            );
        }

        String phoneNumber = request.getPhoneNumber().trim();

        if (phoneNumberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ConflictException(
                    PhoneNumberMessages.NUMBER_ALREADY_EXISTS
            );
        }

        String providerNumberId = request.getProviderNumberId();

        if (providerNumberId != null
                && !providerNumberId.isBlank()
                && phoneNumberRepository.existsByProviderNumberId(
                providerNumberId.trim()
        )) {
            throw new ConflictException(
                    PhoneNumberMessages.PROVIDER_NUMBER_ID_ALREADY_EXISTS
            );
        }

        request.setPhoneNumber(phoneNumber);
        request.setProvider(request.getProvider().trim());

        if (providerNumberId != null) {
            request.setProviderNumberId(providerNumberId.trim());
        }
    }

    /**
     * Validates phone-number updates.
     *
     * @param request update request
     */
    public void validateForUpdate(UpdatePhoneNumberRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Phone number update request is required."
            );
        }

        if (request.getPublicId() == null
                || request.getPublicId().isBlank()) {
            throw new BadRequestException(
                    "Phone number public ID is required."
            );
        }

        if (request.getPhoneNumber() == null
                || request.getPhoneNumber().isBlank()) {
            throw new BadRequestException(
                    "Phone number is required."
            );
        }

        if (request.getAgentPublicId() == null
                || request.getAgentPublicId().isBlank()) {
            throw new BadRequestException(
                    "Agent is required."
            );
        }

        if (request.getProvider() == null
                || request.getProvider().isBlank()) {
            throw new BadRequestException(
                    "Provider is required."
            );
        }

        PhoneNumber existing =
                validateAndGet(request.getPublicId());

        String requestedPhoneNumber =
                request.getPhoneNumber().trim();

        if (!existing.getPhoneNumber().equals(requestedPhoneNumber)
                && phoneNumberRepository.existsByPhoneNumber(
                requestedPhoneNumber
        )) {
            throw new ConflictException(
                    PhoneNumberMessages.NUMBER_ALREADY_EXISTS
            );
        }

        String existingProviderNumberId =
                existing.getProviderNumberId();

        String requestedProviderNumberId =
                request.getProviderNumberId();

        if (requestedProviderNumberId != null
                && !requestedProviderNumberId.isBlank()
                && !requestedProviderNumberId.trim().equals(
                existingProviderNumberId
        )
                && phoneNumberRepository.existsByProviderNumberId(
                requestedProviderNumberId.trim()
        )) {
            throw new ConflictException(
                    PhoneNumberMessages.PROVIDER_NUMBER_ID_ALREADY_EXISTS
            );
        }

        request.setPhoneNumber(requestedPhoneNumber);
        request.setProvider(request.getProvider().trim());

        if (requestedProviderNumberId != null) {
            request.setProviderNumberId(
                    requestedProviderNumberId.trim()
            );
        }
    }

    /**
     * Resolves a non-deleted phone number by public ID.
     *
     * @param publicId phone-number public ID
     * @return non-deleted phone number
     */
    public PhoneNumber validateAndGet(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            throw new BadRequestException(
                    "Phone number public ID is required."
            );
        }

        return phoneNumberRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                PhoneNumberMessages.NOT_FOUND
                        )
                );
    }
}