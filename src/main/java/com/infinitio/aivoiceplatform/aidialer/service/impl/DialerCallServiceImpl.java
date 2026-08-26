package com.infinitio.aivoiceplatform.aidialer.service.impl;

import com.infinitio.aivoiceplatform.aidialer.constant.CallAttemptStatus;
import com.infinitio.aivoiceplatform.aidialer.constant.DialerMessages;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerCallResponse;
import com.infinitio.aivoiceplatform.aidialer.entity.AiDialer;
import com.infinitio.aivoiceplatform.aidialer.entity.DialerCall;
import com.infinitio.aivoiceplatform.aidialer.mapper.AiDialerMapper;
import com.infinitio.aivoiceplatform.aidialer.repository.DialerCallRepository;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallLifecycleService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallRetryService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallValidationService;
import com.infinitio.aivoiceplatform.aidialer.validator.AiDialerValidator;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.campaigncontact.validator.CampaignContactValidator;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Dialer Call.
 *
 * <p>
 * This service acts as the orchestration layer for
 * individual AI Dialer call attempts.
 * </p>
 *
 * <p>
 * Validation, retry management and call lifecycle
 * processing are delegated to their respective services.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DialerCallServiceImpl
        implements DialerCallService {

    private static final Integer NOT_DELETED = 0;

    private final DialerCallRepository
            dialerCallRepository;

    private final AiDialerMapper
            aiDialerMapper;

    private final AiDialerValidator
            aiDialerValidator;

    private final CampaignContactValidator
            campaignContactValidator;

    private final DialerCallValidationService
            dialerCallValidationService;

    private final DialerCallRetryService
            dialerCallRetryService;

    private final DialerCallLifecycleService
            dialerCallLifecycleService;

    /**
     * Creates a new queued Dialer Call.
     *
     * <p>
     * This method only creates the internal queued call.
     * Provider initiation is handled separately by
     * DialerCallInitiationService.
     * </p>
     *
     * @param dialerPublicId AI Dialer public identifier
     * @param campaignContactPublicId campaign contact public identifier
     * @return created dialer call response
     */
    @Override
    public DialerCallResponse createCall(
            String dialerPublicId,
            String campaignContactPublicId) {

        log.info(
                "Creating Dialer Call. "
                        + "Dialer : {}, Campaign Contact : {}",
                dialerPublicId,
                campaignContactPublicId
        );

        dialerCallValidationService.validatePublicIds(
                dialerPublicId,
                campaignContactPublicId
        );

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        dialerPublicId
                );

        CampaignContact campaignContact =
                campaignContactValidator.validateAndGet(
                        campaignContactPublicId
                );

        dialerCallValidationService
                .validateCampaignRelationship(
                        dialer,
                        campaignContact
                );

        dialerCallValidationService
                .validatePhoneNumber(
                        campaignContact
                );

        if (dialerCallRetryService.hasActiveAttempt(
                campaignContact.getId()
        )) {

            throw new BadRequestException(
                    DialerMessages.MAX_ATTEMPTS_REACHED
            );
        }

        if (!dialerCallRetryService.canRetry(
                dialer,
                campaignContact.getId()
        )) {

            throw new BadRequestException(
                    DialerMessages
                            .MAX_ATTEMPTS_REACHED
            );
        }

        List<DialerCall> previousCalls =
                dialerCallRepository
                        .findAllByCampaignContactIdAndIsDeleted(
                                campaignContact.getId(),
                                NOT_DELETED
                        );

        int attemptNumber =
                dialerCallRetryService
                        .calculateNextAttemptNumber(
                                previousCalls
                        );

        DialerCall call =
                DialerCall.builder()
                        .dialer(dialer)
                        .campaignContact(
                                campaignContact
                        )
                        .status(
                                CallAttemptStatus.QUEUED
                        )
                        .attemptNumber(
                                attemptNumber
                        )
                        .phoneNumber(
                                campaignContact
                                        .getPhoneNumber()
                        )
                        .build();

        DialerCall savedCall =
                dialerCallRepository.save(
                        call
                );

        log.info(
                "Dialer Call queued successfully. "
                        + "Public Id : {}, Attempt : {}, "
                        + "Campaign Contact : {}",
                savedCall.getPublicId(),
                savedCall.getAttemptNumber(),
                campaignContactPublicId
        );

        return aiDialerMapper.toCallResponse(
                savedCall
        );
    }

    /**
     * Gets a Dialer Call by public ID.
     *
     * @param publicId dialer call public identifier
     * @return dialer call response
     */
    @Override
    @Transactional(readOnly = true)
    public DialerCallResponse getByPublicId(
            String publicId) {

        DialerCall call =
                getCall(
                        publicId
                );

        return aiDialerMapper.toCallResponse(
                call
        );
    }

    /**
     * Gets all calls for a dialer.
     *
     * @param dialerPublicId AI Dialer public identifier
     * @return dialer call responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<DialerCallResponse> getByDialer(
            String dialerPublicId) {

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        dialerPublicId
                );

        return dialerCallRepository
                .findAllByDialerIdAndIsDeleted(
                        dialer.getId(),
                        NOT_DELETED
                )
                .stream()
                .map(
                        aiDialerMapper::toCallResponse
                )
                .toList();
    }

    /**
     * Gets all call attempts for a campaign contact.
     *
     * @param campaignContactPublicId campaign contact public ID
     * @return dialer call responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<DialerCallResponse>
    getByCampaignContact(
            String campaignContactPublicId) {

        CampaignContact campaignContact =
                campaignContactValidator.validateAndGet(
                        campaignContactPublicId
                );

        return dialerCallRepository
                .findAllByCampaignContactIdAndIsDeleted(
                        campaignContact.getId(),
                        NOT_DELETED
                )
                .stream()
                .map(
                        aiDialerMapper::toCallResponse
                )
                .toList();
    }

    /**
     * Updates Dialer Call status.
     *
     * @param publicId dialer call public identifier
     * @param status new status
     * @return updated call response
     */
    @Override
    public DialerCallResponse updateStatus(
            String publicId,
            CallAttemptStatus status) {

        if (status == null) {

            throw new BadRequestException(
                    DialerMessages
                            .DIALER_CALL_STATUS_REQUIRED
            );
        }

        DialerCall call =
                getCall(
                        publicId
                );

        dialerCallLifecycleService.updateStatus(
                call,
                status
        );

        return aiDialerMapper.toCallResponse(
                call
        );
    }

    /**
     * Updates Exotel Call ID.
     *
     * @param publicId dialer call public identifier
     * @param exotelCallId Exotel call ID
     * @return updated call response
     */
    @Override
    public DialerCallResponse updateExotelCallId(
            String publicId,
            String exotelCallId) {

        if (exotelCallId == null
                || exotelCallId.isBlank()) {

            throw new BadRequestException(
                    DialerMessages
                            .EXOTEL_CALL_ID_REQUIRED
            );
        }

        DialerCall call =
                getCall(
                        publicId
                );

        dialerCallRepository
                .findByExotelCallId(
                        exotelCallId
                )
                .ifPresent(existingCall -> {

                    if (!existingCall
                            .getId()
                            .equals(
                                    call.getId()
                            )) {

                        throw new BadRequestException(
                                DialerMessages
                                        .EXOTEL_CALL_ID_ALREADY_EXISTS
                        );
                    }
                });

        call.setExotelCallId(
                exotelCallId
        );

        DialerCall savedCall =
                dialerCallRepository.save(
                        call
                );

        return aiDialerMapper.toCallResponse(
                savedCall
        );
    }

    /**
     * Updates flow execution public ID.
     *
     * @param publicId dialer call public identifier
     * @param flowExecutionPublicId flow execution public ID
     * @return updated call response
     */
    @Override
    public DialerCallResponse updateFlowExecution(
            String publicId,
            String flowExecutionPublicId) {

        if (flowExecutionPublicId == null
                || flowExecutionPublicId.isBlank()) {

            throw new BadRequestException(
                    DialerMessages
                            .FLOW_EXECUTION_PUBLIC_ID_REQUIRED
            );
        }

        DialerCall call =
                getCall(
                        publicId
                );

        call.setFlowExecutionPublicId(
                flowExecutionPublicId
        );

        DialerCall savedCall =
                dialerCallRepository.save(
                        call
                );

        return aiDialerMapper.toCallResponse(
                savedCall
        );
    }

    /**
     * Marks a call as answered.
     *
     * @param publicId dialer call public ID
     * @return updated call response
     */
    @Override
    public DialerCallResponse markAnswered(
            String publicId) {

        DialerCall call =
                getCall(
                        publicId
                );

        dialerCallLifecycleService.markAnswered(
                call
        );

        return aiDialerMapper.toCallResponse(
                call
        );
    }

    /**
     * Completes a call.
     *
     * @param publicId dialer call public ID
     * @param durationSeconds call duration
     * @param hangupReason hangup reason
     * @return updated call response
     */
    @Override
    public DialerCallResponse completeCall(
            String publicId,
            Integer durationSeconds,
            String hangupReason) {

        if (durationSeconds != null
                && durationSeconds < 0) {

            throw new BadRequestException(
                    DialerMessages
                            .DURATION_NEGATIVE
            );
        }

        DialerCall call =
                getCall(
                        publicId
                );

        dialerCallLifecycleService.completeCall(
                call,
                durationSeconds,
                hangupReason
        );

        return aiDialerMapper.toCallResponse(
                call
        );
    }

    /**
     * Marks a call as failed.
     *
     * @param publicId dialer call public ID
     * @param failureReason failure reason
     * @return updated call response
     */
    @Override
    public DialerCallResponse failCall(
            String publicId,
            String failureReason) {

        DialerCall call =
                getCall(
                        publicId
                );

        dialerCallLifecycleService.failCall(
                call,
                failureReason
        );

        return aiDialerMapper.toCallResponse(
                call
        );
    }

    /**
     * Determines whether another attempt is allowed.
     *
     * @param dialerPublicId AI Dialer public ID
     * @param campaignContactPublicId campaign contact public ID
     * @return true if retry is allowed
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canRetry(
            String dialerPublicId,
            String campaignContactPublicId) {

        dialerCallValidationService.validatePublicIds(
                dialerPublicId,
                campaignContactPublicId
        );

        AiDialer dialer =
                aiDialerValidator.validateAndGet(
                        dialerPublicId
                );

        CampaignContact campaignContact =
                campaignContactValidator.validateAndGet(
                        campaignContactPublicId
                );

        dialerCallValidationService
                .validateCampaignRelationship(
                        dialer,
                        campaignContact
                );

        return dialerCallRetryService.canRetry(
                dialer,
                campaignContact.getId()
        );
    }

    /**
     * Resolves a Dialer Call by public ID.
     *
     * @param publicId dialer call public ID
     * @return dialer call entity
     */
    private DialerCall getCall(
            String publicId) {

        if (publicId == null
                || publicId.isBlank()) {

            throw new BadRequestException(
                    DialerMessages
                            .DIALER_CALL_PUBLIC_ID_REQUIRED
            );
        }

        return dialerCallRepository
                .findByPublicIdAndIsDeleted(
                        publicId,
                        NOT_DELETED
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                DialerMessages
                                        .DIALER_CALL_NOT_FOUND
                        )
                );
    }
}