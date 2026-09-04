package com.infinitio.aivoiceplatform.call.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.call.dto.request.CreateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.request.UpdateCallRequest;
import com.infinitio.aivoiceplatform.call.dto.response.CallResponse;
import com.infinitio.aivoiceplatform.call.entity.Call;
import com.infinitio.aivoiceplatform.call.mapper.CallMapper;
import com.infinitio.aivoiceplatform.call.repository.CallRepository;
import com.infinitio.aivoiceplatform.call.service.CallService;
import com.infinitio.aivoiceplatform.call.validator.CallValidator;
import com.infinitio.aivoiceplatform.campaigncontact.entity.CampaignContact;
import com.infinitio.aivoiceplatform.campaigncontact.validator.CampaignContactValidator;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Call.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallServiceImpl
        implements CallService {

    private static final Integer NOT_DELETED = 0;

    private final CallRepository callRepository;

    private final CallMapper callMapper;

    private final CallValidator callValidator;

    private final CampaignContactValidator
            campaignContactValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public CallResponse create(
            CreateCallRequest request) {

        log.info(
                "Creating Call. Campaign Contact : {}, Provider : {}",
                request.getCampaignContactPublicId(),
                request.getProvider()
        );

        CampaignContact campaignContact =
                campaignContactValidator.validateAndGet(
                        request.getCampaignContactPublicId()
                );

        callValidator.validateForCreate(
                request
        );

        Call call =
                callMapper.toEntity(
                        request
                );

        call.setCreatedBy(
                currentUserService.getCurrentUserId()
        );

        call.setCampaignContact(
                campaignContact
        );

        Call savedCall =
                callRepository.save(
                        call
                );

        log.info(
                "Call created successfully. Public Id : {}",
                savedCall.getPublicId()
        );

        return callMapper.toResponse(
                savedCall
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public CallResponse update(
            UpdateCallRequest request) {

        log.info(
                "Updating Call. Public Id : {}",
                request.getPublicId()
        );

        callValidator.validateForUpdate(
                request
        );

        Call call =
                callValidator.validateAndGet(
                        request.getPublicId()
                );

        CampaignContact campaignContact =
                campaignContactValidator.validateAndGet(
                        request.getCampaignContactPublicId()
                );

        callMapper.updateEntity(
                request,
                call
        );

        call.setCampaignContact(
                campaignContact
        );

        Call updatedCall =
                callRepository.save(
                        call
                );

        log.info(
                "Call updated successfully. Public Id : {}",
                updatedCall.getPublicId()
        );

        return callMapper.toResponse(
                updatedCall
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CallResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Call. Public Id : {}",
                publicId
        );

        Call call =
                callValidator.validateAndGet(
                        publicId
                );

        return callMapper.toResponse(
                call
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CallResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Calls. Page : {}, Size : {}",
                page,
                size
        );

        Page<Call> result =
                callRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return buildPageResponse(result);
    }


    // =========================================================
    // GET BY CAMPAIGN CONTACT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CallResponse>
    getByCampaignContact(
            String campaignContactPublicId,
            int page,
            int size) {

        log.info(
                "Fetching Calls for Campaign Contact : {}, Page : {}, Size : {}",
                campaignContactPublicId,
                page,
                size
        );

        CampaignContact campaignContact =
                campaignContactValidator.validateAndGet(
                        campaignContactPublicId
                );

        Page<Call> result =
                callRepository
                        .findByCampaignContactIdAndIsDeleted(
                                campaignContact.getId(),
                                NOT_DELETED,
                                PageRequest.of(
                                        page,
                                        size
                                )
                        );

        return buildPageResponse(result);
    }


    // =========================================================
    // BUILD PAGE RESPONSE
    // =========================================================

    private PageResponse<CallResponse>
    buildPageResponse(
            Page<Call> result) {

        return PageResponse
                .<CallResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        callMapper::toResponse
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
                "Deleting Call. Public Id : {}",
                publicId
        );

        Call call =
                callValidator.validateAndGet(
                        publicId
                );

        call.markAsDeleted(
                1L
        );

        callRepository.save(
                call
        );

        log.info(
                "Call deleted successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Call. Public Id : {}",
                publicId
        );

        Call call =
                callValidator.validateAndGet(
                        publicId
                );

        call.activate(
                1L
        );

        callRepository.save(
                call
        );

        log.info(
                "Call activated successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Call. Public Id : {}",
                publicId
        );

        Call call =
                callValidator.validateAndGet(
                        publicId
                );

        call.deactivate(
                1L
        );

        callRepository.save(
                call
        );

        log.info(
                "Call deactivated successfully. Public Id : {}",
                publicId
        );
    }
}