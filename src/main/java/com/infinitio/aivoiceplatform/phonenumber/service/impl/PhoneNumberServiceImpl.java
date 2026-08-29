package com.infinitio.aivoiceplatform.phonenumber.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.CreatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.request.UpdatePhoneNumberRequest;
import com.infinitio.aivoiceplatform.phonenumber.dto.response.PhoneNumberResponse;
import com.infinitio.aivoiceplatform.phonenumber.entity.PhoneNumber;
import com.infinitio.aivoiceplatform.phonenumber.mapper.PhoneNumberMapper;
import com.infinitio.aivoiceplatform.phonenumber.repository.PhoneNumberRepository;
import com.infinitio.aivoiceplatform.phonenumber.service.PhoneNumberService;
import com.infinitio.aivoiceplatform.phonenumber.validator.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Phone Number.
 *
 * Handles phone-number registration and lifecycle operations.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PhoneNumberServiceImpl
        implements PhoneNumberService {

    /**
     * System user used only when an operation is executed
     * without an authenticated user.
     *
     * Normal Swagger/UI requests use the authenticated
     * user's actual database ID.
     */
    private static final Long SYSTEM_USER_ID = 1L;

    private final PhoneNumberRepository phoneNumberRepository;

    private final PhoneNumberMapper phoneNumberMapper;

    private final PhoneNumberValidator phoneNumberValidator;

    private final AgentValidator agentValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE PHONE NUMBER
    // =========================================================

    @Override
    public PhoneNumberResponse create(
            CreatePhoneNumberRequest request) {

        log.info(
                "Creating Phone Number. Number : {}, Provider : {}",
                request != null
                        ? request.getPhoneNumber()
                        : null,
                request != null
                        ? request.getProvider()
                        : null
        );

        /*
         * Validate request according to the existing
         * PhoneNumberValidator implementation.
         */
        phoneNumberValidator.validateForCreate(
                request
        );

        /*
         * Validate and load the Agent.
         */
        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        /*
         * Resolve the authenticated user.
         *
         * phone_numbers.created_by is NOT NULL in the DB,
         * therefore this value must always be populated.
         */
        Long currentUserId =
                resolveCurrentUserId();

        /*
         * Convert request into entity using the existing mapper.
         */
        PhoneNumber phoneNumber =
                phoneNumberMapper.toEntity(
                        request
                );

        /*
         * Set the Agent relationship.
         */
        phoneNumber.setAgent(
                agent
        );

        /*
         * Set audit information.
         *
         * This is the important fix for:
         *
         * Column 'created_by' cannot be null
         */
        phoneNumber.setCreatedBy(
                currentUserId
        );

        /*
         * Save.
         */
        PhoneNumber savedPhoneNumber =
                phoneNumberRepository.save(
                        phoneNumber
                );

        log.info(
                "Phone Number created successfully. " +
                        "Public Id : {}, Created By : {}",
                savedPhoneNumber.getPublicId(),
                currentUserId
        );

        return phoneNumberMapper.toResponse(
                savedPhoneNumber
        );
    }


    // =========================================================
    // UPDATE PHONE NUMBER
    // =========================================================

    @Override
    public PhoneNumberResponse update(
            UpdatePhoneNumberRequest request) {

        log.info(
                "Updating Phone Number. Public Id : {}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        phoneNumberValidator.validateForUpdate(
                request
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        request.getPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        phoneNumberMapper.updateEntity(
                request,
                phoneNumber
        );

        phoneNumber.setAgent(
                agent
        );

        /*
         * Update audit information.
         */
        Long currentUserId =
                resolveCurrentUserId();

        phoneNumber.setUpdatedBy(
                currentUserId
        );

        PhoneNumber updatedPhoneNumber =
                phoneNumberRepository.save(
                        phoneNumber
                );

        log.info(
                "Phone Number updated successfully. " +
                        "Public Id : {}, Updated By : {}",
                updatedPhoneNumber.getPublicId(),
                currentUserId
        );

        return phoneNumberMapper.toResponse(
                updatedPhoneNumber
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PhoneNumberResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Phone Number. Public Id : {}",
                publicId
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        publicId
                );

        return phoneNumberMapper.toResponse(
                phoneNumber
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PhoneNumberResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Phone Numbers. Page : {}, Size : {}",
                page,
                size
        );

        Page<PhoneNumber> result =
                phoneNumberRepository.findAll(
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<PhoneNumberResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        phoneNumberMapper::toResponse
                                )
                                .toList()
                )
                .pageNumber(
                        result.getNumber()
                )
                .pageSize(
                        result.getSize()
                )
                .totalPages(
                        result.getTotalPages()
                )
                .totalElements(
                        result.getTotalElements()
                )
                .first(
                        result.isFirst()
                )
                .last(
                        result.isLast()
                )
                .build();
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Phone Number. Public Id : {}",
                publicId
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                resolveCurrentUserId();

        /*
         * Existing entity lifecycle method.
         *
         * The hardcoded 1L has been replaced with
         * the authenticated user ID.
         */
        phoneNumber.markAsDeleted(
                currentUserId
        );

        phoneNumberRepository.save(
                phoneNumber
        );

        log.info(
                "Phone Number deleted successfully. " +
                        "Public Id : {}, Deleted By : {}",
                publicId,
                currentUserId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Phone Number. Public Id : {}",
                publicId
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                resolveCurrentUserId();

        /*
         * Existing entity lifecycle method.
         */
        phoneNumber.activate(
                currentUserId
        );

        phoneNumberRepository.save(
                phoneNumber
        );

        log.info(
                "Phone Number activated successfully. " +
                        "Public Id : {}, Updated By : {}",
                publicId,
                currentUserId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Phone Number. Public Id : {}",
                publicId
        );

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(
                        publicId
                );

        Long currentUserId =
                resolveCurrentUserId();

        /*
         * Existing entity lifecycle method.
         */
        phoneNumber.deactivate(
                currentUserId
        );

        phoneNumberRepository.save(
                phoneNumber
        );

        log.info(
                "Phone Number deactivated successfully. " +
                        "Public Id : {}, Updated By : {}",
                publicId,
                currentUserId
        );
    }


    // =========================================================
    // CURRENT USER
    // =========================================================

    /**
     * Resolves the user responsible for the operation.
     *
     * Priority:
     *
     * 1. Authenticated user's database ID.
     * 2. System user when the operation is executed
     *    without an authenticated request.
     *
     * This follows the same pattern already used by
     * RuntimePersistenceService.
     */
    private Long resolveCurrentUserId() {

        try {

            if (currentUserService.isAuthenticated()) {

                Long currentUserId =
                        currentUserService
                                .getCurrentUserId();

                if (currentUserId != null) {

                    return currentUserId;
                }
            }

        } catch (Exception exception) {

            log.warn(
                    "Unable to resolve authenticated user. " +
                            "Using system user.",
                    exception
            );
        }

        return SYSTEM_USER_ID;
    }
}