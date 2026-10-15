package com.infinitio.aivoiceplatform.call.service.impl;

import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.call.constant.CallMessages;
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
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.campaign.validator.CampaignValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for call management.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallServiceImpl implements CallService {

    private static final Integer NOT_DELETED = 0;

    private final CallRepository callRepository;
    private final CallMapper callMapper;
    private final CallValidator callValidator;
    private final CampaignContactValidator campaignContactValidator;
    private final TenantAccessService tenantAccessService;
    private final CampaignValidator campaignValidator;

    @Override
    public CallResponse create(CreateCallRequest request) {

        callValidator.validateForCreate(request);

        CampaignContact campaignContact =
                campaignContactValidator.validateAndGet(
                        request.getCampaignContactPublicId()
                );

        tenantAccessService.validateCampaignAccess(
                campaignContact.getCampaign()
        );

        Call call = callMapper.toEntity(request);
        call.setCampaignContact(campaignContact);
        call.setCreatedBy(requireCurrentUserId());

        Call savedCall = callRepository.save(call);

        log.info(
                "Call created. publicId={}, campaignContactPublicId={}",
                savedCall.getPublicId(),
                campaignContact.getPublicId()
        );

        return callMapper.toResponse(savedCall);
    }

    @Override
    public CallResponse update(UpdateCallRequest request) {

        callValidator.validateForUpdate(request);

        Call existingCall =
                callValidator.validateAndGet(request.getPublicId());

        validateCallAccess(existingCall);

        CampaignContact targetContact =
                campaignContactValidator.validateAndGet(
                        request.getCampaignContactPublicId()
                );

        tenantAccessService.validateCampaignAccess(
                targetContact.getCampaign()
        );

        callMapper.updateEntity(request, existingCall);
        existingCall.setCampaignContact(targetContact);
        existingCall.setUpdatedBy(requireCurrentUserId());

        Call savedCall = callRepository.save(existingCall);

        log.info("Call updated. publicId={}", savedCall.getPublicId());

        return callMapper.toResponse(savedCall);
    }

    @Override
    @Transactional(readOnly = true)
    public CallResponse getByPublicId(String publicId) {

        Call call = callValidator.validateAndGet(publicId);
        validateCallAccess(call);

        return callMapper.toResponse(call);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CallResponse> getAll() {

        List<Call> calls;

        if (tenantAccessService.isSuperAdmin()) {
            calls = callRepository
                    .findAllByIsDeletedOrderByCreatedAtDesc(
                            NOT_DELETED
                    );
        } else {
            Long tenantId = requireCurrentTenantId();

            calls = callRepository.findAllByTenantIdAndIsDeleted(
                    tenantId,
                    NOT_DELETED
            );
        }

        return toResponses(calls);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CallResponse> getByCampaign(
            String campaignPublicId) {

        /*
         * Resolve the campaign and verify the current user's access.
         */
        var campaign =
                campaignValidator.validateAndGet(campaignPublicId);

        tenantAccessService.validateCampaignAccess(campaign);

        /*
         * Fetch only calls associated with the selected campaign.
         */
        List<Call> calls;

        if (tenantAccessService.isSuperAdmin()) {

            calls = callRepository.findAllByCampaignIdAndIsDeleted(
                    campaign.getId(),
                    NOT_DELETED
            );

        } else {

            Long tenantId = requireCurrentTenantId();

            calls = callRepository
                    .findAllByCampaignIdAndTenantIdAndIsDeleted(
                            campaign.getId(),
                            tenantId,
                            NOT_DELETED
                    );
        }

        return toResponses(calls);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CallResponse> getByTenantId(Long tenantId) {

        tenantAccessService.validateSuperAdminAccess();

        if (tenantId == null || tenantId <= 0) {
            throw new BadRequestException(
                    CallMessages.INVALID_TENANT_ID
            );
        }

        return toResponses(
                callRepository.findAllByTenantIdAndIsDeleted(
                        tenantId,
                        NOT_DELETED
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CallResponse> getByCampaignContact(
            String campaignContactPublicId) {

        CampaignContact contact =
                campaignContactValidator.validateAndGet(
                        campaignContactPublicId
                );

        tenantAccessService.validateCampaignAccess(
                contact.getCampaign()
        );

        List<Call> calls;

        if (tenantAccessService.isSuperAdmin()) {
            calls = callRepository
                    .findAllByCampaignContactIdAndIsDeleted(
                            contact.getId(),
                            NOT_DELETED
                    );
        } else {
            calls = callRepository
                    .findAllByCampaignContactIdAndTenantIdAndIsDeleted(
                            contact.getId(),
                            requireCurrentTenantId(),
                            NOT_DELETED
                    );
        }

        return toResponses(calls);
    }

    @Override
    public void delete(String publicId) {

        Call call = callValidator.validateAndGet(publicId);
        validateCallAccess(call);

        call.markAsDeleted(requireCurrentUserId());
        callRepository.save(call);

        log.info("Call soft-deleted. publicId={}", publicId);
    }

    @Override
    public void activate(String publicId) {

        Call call = callValidator.validateAndGet(publicId);
        validateCallAccess(call);

        call.activate(requireCurrentUserId());
        callRepository.save(call);
    }

    @Override
    public void deactivate(String publicId) {

        Call call = callValidator.validateAndGet(publicId);
        validateCallAccess(call);

        call.deactivate(requireCurrentUserId());
        callRepository.save(call);
    }

    /**
     * Validates access to a call through its campaign or creator tenant.
     *
     * Direct calls without a campaign contact are authorized through the
     * tenant of the user who created the call.
     */
    private void validateCallAccess(Call call) {

        if (call == null) {
            throw new ForbiddenException(
                    "Call information is unavailable."
            );
        }

        if (tenantAccessService.isSuperAdmin()) {
            return;
        }

        if (call.getCampaignContact() != null) {
            tenantAccessService.validateCampaignAccess(
                    call.getCampaignContact().getCampaign()
            );
            return;
        }

        Long tenantId = requireCurrentTenantId();

        boolean accessible =
                call.getId() != null
                        && callRepository.existsAccessibleByIdAndTenantId(
                        call.getId(),
                        tenantId,
                        NOT_DELETED
                );

        if (!accessible) {
            throw new ForbiddenException(
                    "You do not have permission to access this call."
            );
        }
    }

    private Long requireCurrentTenantId() {

        Long tenantId = tenantAccessService.getCurrentTenantId();

        if (tenantId == null) {
            throw new ForbiddenException(
                    "Current user's tenant is unavailable."
            );
        }

        return tenantId;
    }

    private Long requireCurrentUserId() {

        Long userId = tenantAccessService.getCurrentUserId();

        if (userId == null) {
            throw new ForbiddenException(
                    "Authenticated user information is unavailable."
            );
        }

        return userId;
    }

    private List<CallResponse> toResponses(List<Call> calls) {

        return calls.stream()
                .map(callMapper::toResponse)
                .toList();
    }
}