package com.infinitio.aivoiceplatform.call.service.impl;

import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
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
import com.infinitio.aivoiceplatform.master.role.constant.RoleConstants;
import com.infinitio.aivoiceplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

        validateTenantAccess(
                call
        );

        return callMapper.toResponse(
                call
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CallResponse> getAll() {

        User currentUser =
                currentUserService.getCurrentUser();

        String roleCode =
                currentUser
                        .getRole()
                        .getRoleCode();

        log.info(
                "Fetching Calls for authenticated role."
        );

        List<Call> calls;

        if (RoleConstants.SUPER_ADMIN.equalsIgnoreCase(
                roleCode)) {

            log.info(
                    "SUPER_ADMIN access granted for all tenant calls."
            );

            calls =
                    callRepository
                            .findAllByIsDeletedOrderByCreatedAtDesc(
                                    NOT_DELETED
                            );

        } else {

            Long tenantId =
                    currentUser
                            .getTenant()
                            .getId();

            log.info(
                    "Fetching Calls for authenticated tenant."
            );

            calls =
                    callRepository
                            .findAllByTenantIdAndIsDeleted(
                                    tenantId,
                                    NOT_DELETED
                            );
        }

        return calls
                .stream()
                .map(
                        callMapper::toResponse
                )
                .toList();
    }


    // =========================================================
    // GET BY TENANT ID
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CallResponse> getByTenantId(
            Long tenantId) {

        validateSuperAdminAccess();

        if (tenantId == null
                || tenantId <= 0) {

            throw new BadRequestException(
                    CallMessages.INVALID_TENANT_ID
            );
        }

        log.info(
                "SUPER_ADMIN fetching Calls for selected tenant."
        );

        List<Call> calls =
                callRepository
                        .findAllByTenantIdAndIsDeleted(
                                tenantId,
                                NOT_DELETED
                        );

        return calls
                .stream()
                .map(
                        callMapper::toResponse
                )
                .toList();
    }


    // =========================================================
    // GET BY CAMPAIGN CONTACT
    // =========================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<CallResponse> getByCampaignContact(
            String campaignContactPublicId) {

        log.info(
                "Fetching Calls for Campaign Contact."
        );

        CampaignContact campaignContact =
                campaignContactValidator.validateAndGet(
                        campaignContactPublicId
                );

        Long tenantId =
                currentUserService
                        .getCurrentUser()
                        .getTenant()
                        .getId();

        List<Call> calls =
                callRepository
                        .findAllByCampaignContactIdAndTenantIdAndIsDeleted(
                                campaignContact.getId(),
                                tenantId,
                                NOT_DELETED
                        );

        return calls
                .stream()
                .map(
                        callMapper::toResponse
                )
                .toList();
    }


    // =========================================================
    // TENANT ACCESS VALIDATION
    // =========================================================

    /**
     * Validates whether the current user can access
     * the supplied Call.
     *
     * @param call call being accessed
     */
    private void validateTenantAccess(
            Call call) {

        User currentUser =
                currentUserService.getCurrentUser();

        String roleCode =
                currentUser
                        .getRole()
                        .getRoleCode();

        /*
         * SUPER_ADMIN can access calls belonging
         * to any tenant.
         */
        if (RoleConstants.SUPER_ADMIN.equalsIgnoreCase(
                roleCode)) {

            return;
        }

        Long currentTenantId =
                currentUser
                        .getTenant()
                        .getId();

        boolean accessible =
                isCallAccessibleToTenant(
                        call,
                        currentTenantId
                );

        if (!accessible) {

            throw new ForbiddenException(
                    "You do not have permission to access this call."
            );
        }
    }


    /**
     * Determines whether a Call belongs to the supplied tenant.
     *
     * @param call call being checked
     * @param tenantId tenant database identifier
     * @return true when the call belongs to the tenant
     */
    private boolean isCallAccessibleToTenant(
            Call call,
            Long tenantId) {

        if (call.getCampaignContact() != null
                && call.getCampaignContact()
                .getCampaign() != null
                && call.getCampaignContact()
                .getCampaign()
                .getAgent() != null
                && call.getCampaignContact()
                .getCampaign()
                .getAgent()
                .getTenant() != null) {

            return tenantId.equals(
                    call.getCampaignContact()
                            .getCampaign()
                            .getAgent()
                            .getTenant()
                            .getId()
            );
        }

        return call.getCreatedBy() != null
                && currentUserService
                .getCurrentUserId()
                .equals(call.getCreatedBy());
    }


    /**
     * Validates that the authenticated user is SUPER_ADMIN.
     *
     * @throws ForbiddenException when the current user is not SUPER_ADMIN
     */
    private void validateSuperAdminAccess() {

        User currentUser =
                currentUserService.getCurrentUser();

        String roleCode =
                currentUser
                        .getRole()
                        .getRoleCode();

        if (!RoleConstants.SUPER_ADMIN.equalsIgnoreCase(
                roleCode)) {

            log.warn(
                    "Unauthorized tenant call access attempt."
            );

            throw new ForbiddenException(
                    CallMessages.SUPER_ADMIN_REQUIRED
            );
        }
    }


    // =========================================================
    // DELETE
    // =========================================================

    /**
     * {@inheritDoc}
     */
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
                currentUserService.getCurrentUserId()
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

    /**
     * {@inheritDoc}
     */
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
                currentUserService.getCurrentUserId()
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

    /**
     * {@inheritDoc}
     */
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
                currentUserService.getCurrentUserId()
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