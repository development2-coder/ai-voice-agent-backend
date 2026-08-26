package com.infinitio.aivoiceplatform.call.validator;

import com.infinitio.aivoiceplatform.call.constant.CallMessages;
import com.infinitio.aivoiceplatform.call.dto.request.CreateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.request.UpdateCallRequest;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ConflictException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for Call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CallValidator {

    private static final Integer NOT_DELETED = 0;

    private final CallRepository callRepository;


    // =========================================================
    // CREATE
    // =========================================================

    public void validateForCreate(
            CreateCallRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Call request cannot be null."
            );
        }

        /*
         * providerCallId is optional.
         *
         * If provided, it must be globally unique because
         * the database column is UNIQUE.
         */
        if (request.getProviderCallId() != null
                && !request.getProviderCallId().isBlank()
                && callRepository.existsByProviderCallId(
                request.getProviderCallId().trim()
        )) {

            throw new ConflictException(
                    CallMessages.PROVIDER_CALL_ID_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    public void validateForUpdate(
            UpdateCallRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Call request cannot be null."
            );
        }

        if (request.getPublicId() == null
                || request.getPublicId().isBlank()) {

            throw new BadRequestException(
                    "Call public ID is required."
            );
        }

        Call existing =
                validateAndGet(
                        request.getPublicId()
                );

        String requestProviderCallId =
                request.getProviderCallId();

        if (requestProviderCallId != null
                && !requestProviderCallId.isBlank()
                && !requestProviderCallId.equals(
                existing.getProviderCallId()
        )
                && callRepository
                .existsByProviderCallIdAndIdNot(
                        requestProviderCallId.trim(),
                        existing.getId()
                )) {

            throw new ConflictException(
                    CallMessages.PROVIDER_CALL_ID_ALREADY_EXISTS
            );
        }
    }


    // =========================================================
    // GET
    // =========================================================

    public Call validateAndGet(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    "Call public ID is required."
            );
        }

        return callRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                CallMessages.NOT_FOUND
                        )
                );
    }
}