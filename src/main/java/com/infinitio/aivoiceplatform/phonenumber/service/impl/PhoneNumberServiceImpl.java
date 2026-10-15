package com.infinitio.aivoiceplatform.phonenumber.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.BadRequestException;
import com.infinitio.aivoiceplatform.exception.ForbiddenException;
import com.infinitio.aivoiceplatform.phonenumber.constant.PhoneNumberMessages;
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
 * Service implementation for phone-number management.
 *
 * Applies RBAC-adjacent tenant isolation through TenantAccessService.
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PhoneNumberServiceImpl implements PhoneNumberService {

    private static final Integer NOT_DELETED = 0;

    private final PhoneNumberRepository phoneNumberRepository;

    private final PhoneNumberMapper phoneNumberMapper;

    private final PhoneNumberValidator phoneNumberValidator;

    private final AgentValidator agentValidator;

    private final TenantAccessService tenantAccessService;

    /**
     * Creates a phone number for an accessible agent.
     *
     * @param request create request
     * @return created phone number
     */
    @Override
    public PhoneNumberResponse create(
            CreatePhoneNumberRequest request) {

        phoneNumberValidator.validateForCreate(request);

        Agent agent = agentValidator.validateAndGet(
                request.getAgentPublicId()
        );

        tenantAccessService.validateAgentAccess(agent);

        PhoneNumber phoneNumber =
                phoneNumberMapper.toEntity(request);

        phoneNumber.setAgent(agent);
        phoneNumber.setCreatedBy(requireCurrentUserId());

        PhoneNumber savedPhoneNumber =
                phoneNumberRepository.save(phoneNumber);

        log.info(
                "Phone number created. Public ID: {}",
                savedPhoneNumber.getPublicId()
        );

        return phoneNumberMapper.toResponse(savedPhoneNumber);
    }

    /**
     * Updates a phone number only when both the existing number
     * and the target agent belong to an accessible tenant.
     *
     * @param request update request
     * @return updated phone number
     */
    @Override
    public PhoneNumberResponse update(
            UpdatePhoneNumberRequest request) {

        if (request == null) {
            throw new BadRequestException(
                    "Phone number update request is required."
            );
        }

        PhoneNumber existingPhoneNumber =
                phoneNumberValidator.validateAndGet(
                        request.getPublicId()
                );

        tenantAccessService.validateAgentAccess(
                existingPhoneNumber.getAgent()
        );

        phoneNumberValidator.validateForUpdate(request);

        Agent targetAgent = agentValidator.validateAndGet(
                request.getAgentPublicId()
        );

        tenantAccessService.validateAgentAccess(targetAgent);

        phoneNumberMapper.updateEntity(
                request,
                existingPhoneNumber
        );

        existingPhoneNumber.setAgent(targetAgent);
        existingPhoneNumber.setUpdatedBy(requireCurrentUserId());

        PhoneNumber savedPhoneNumber =
                phoneNumberRepository.save(existingPhoneNumber);

        log.info(
                "Phone number updated. Public ID: {}",
                savedPhoneNumber.getPublicId()
        );

        return phoneNumberMapper.toResponse(savedPhoneNumber);
    }

    /**
     * Fetches a phone number after validating tenant access.
     *
     * @param publicId phone-number public ID
     * @return phone-number response
     */
    @Override
    @Transactional(readOnly = true)
    public PhoneNumberResponse getByPublicId(String publicId) {

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(publicId);

        tenantAccessService.validateAgentAccess(
                phoneNumber.getAgent()
        );

        return phoneNumberMapper.toResponse(phoneNumber);
    }

    /**
     * Lists phone numbers visible to the current user.
     *
     * SUPER_ADMIN can list across tenants. ADMIN and USER are
     * restricted to records belonging to their current tenant.
     *
     * @param page zero-based page number
     * @param size page size
     * @return tenant-scoped phone-number page
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PhoneNumberResponse> getAll(
            int page,
            int size) {

        if (page < 0) {
            throw new BadRequestException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > 200) {
            throw new BadRequestException(
                    "Page size must be between 1 and 200."
            );
        }

        PageRequest pageable = PageRequest.of(page, size);

        Page<PhoneNumber> result;

        if (tenantAccessService.isSuperAdmin()) {
            result = phoneNumberRepository.findByIsDeleted(
                    NOT_DELETED,
                    pageable
            );
        } else {
            Long tenantId =
                    tenantAccessService.getCurrentTenantId();

            if (tenantId == null) {
                throw new ForbiddenException(
                        "Current user's tenant is unavailable."
                );
            }

            result =
                    phoneNumberRepository
                            .findByAgent_Tenant_IdAndIsDeleted(
                                    tenantId,
                                    NOT_DELETED,
                                    pageable
                            );
        }

        return PageResponse.<PhoneNumberResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(phoneNumberMapper::toResponse)
                                .toList()
                )
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    /**
     * Soft-deletes a phone number after checking tenant access.
     *
     * @param publicId phone-number public ID
     */
    @Override
    public void delete(String publicId) {

        PhoneNumber phoneNumber =
                getAuthorizedPhoneNumber(publicId);

        phoneNumber.markAsDeleted(requireCurrentUserId());

        phoneNumberRepository.save(phoneNumber);

        log.info("Phone number deleted. Public ID: {}", publicId);
    }

    /**
     * Activates a phone number after checking tenant access.
     *
     * @param publicId phone-number public ID
     */
    @Override
    public void activate(String publicId) {

        PhoneNumber phoneNumber =
                getAuthorizedPhoneNumber(publicId);

        phoneNumber.activate(requireCurrentUserId());

        phoneNumberRepository.save(phoneNumber);

        log.info("Phone number activated. Public ID: {}", publicId);
    }

    /**
     * Deactivates a phone number after checking tenant access.
     *
     * @param publicId phone-number public ID
     */
    @Override
    public void deactivate(String publicId) {

        PhoneNumber phoneNumber =
                getAuthorizedPhoneNumber(publicId);

        phoneNumber.deactivate(requireCurrentUserId());

        phoneNumberRepository.save(phoneNumber);

        log.info("Phone number deactivated. Public ID: {}", publicId);
    }

    /**
     * Retrieves a phone number and validates its agent's tenant.
     *
     * @param publicId phone-number public ID
     * @return authorized phone number
     */
    private PhoneNumber getAuthorizedPhoneNumber(String publicId) {

        PhoneNumber phoneNumber =
                phoneNumberValidator.validateAndGet(publicId);

        tenantAccessService.validateAgentAccess(
                phoneNumber.getAgent()
        );

        return phoneNumber;
    }

    /**
     * Returns the authenticated user's database ID.
     * Management operations must not silently fall back to a
     * hardcoded system-user ID.
     *
     * @return current user database ID
     */
    private Long requireCurrentUserId() {

        Long currentUserId =
                tenantAccessService.getCurrentUserId();

        if (currentUserId == null) {
            throw new ForbiddenException(
                    "Authenticated user information is unavailable."
            );
        }

        return currentUserId;
    }
}