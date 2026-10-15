package com.infinitio.aivoiceplatform.tts.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.tts.dto.request.CreateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.request.UpdateTtsRequest;
import com.infinitio.aivoiceplatform.tts.dto.response.TtsResponse;
import com.infinitio.aivoiceplatform.tts.entity.Tts;
import com.infinitio.aivoiceplatform.tts.mapper.TtsMapper;
import com.infinitio.aivoiceplatform.tts.repository.TtsRepository;
import com.infinitio.aivoiceplatform.tts.service.TtsService;
import com.infinitio.aivoiceplatform.tts.validator.TtsValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.NOT_DELETED;

/**
 * Service implementation for TTS.
 *
 * <p>
 * Handles TTS configuration CRUD operations together with
 * RBAC-compatible tenant isolation and audit handling.
 * </p>
 *
 * <p>
 * Tenant relationship:
 *
 * <pre>
 * TTS
 *  ↓
 * Agent
 *  ↓
 * Tenant
 * </pre>
 *
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TtsServiceImpl implements TtsService {

    private final TtsRepository ttsRepository;

    private final TtsMapper ttsMapper;

    private final TtsValidator ttsValidator;

    private final AgentValidator agentValidator;

    private final TenantAccessService tenantAccessService;

    /**
     * Creates a new TTS configuration.
     *
     * @param request create TTS request
     * @return created TTS response
     */
    @Override
    public TtsResponse create(
            CreateTtsRequest request) {

        log.info(
                "Creating TTS. Code : {}, Provider : {}, Model : {}",
                request != null
                        ? request.getTtsCode()
                        : null,
                request != null
                        ? request.getProvider()
                        : null,
                request != null
                        ? request.getModel()
                        : null
        );

        /*
         * Validate TTS request.
         */
        ttsValidator.validateForCreate(
                request
        );

        /*
         * Resolve the requested Agent.
         */
        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        /*
         * SUPER_ADMIN can access any tenant.
         *
         * ADMIN and USER can only use an Agent
         * belonging to their current tenant.
         */
        tenantAccessService.validateAgentAccess(
                agent
        );

        Tts tts =
                ttsMapper.toEntity(
                        request
                );

        tts.setAgent(
                agent
        );

        /*
         * Audit information comes from the
         * authenticated application user.
         */
        tts.setCreatedBy(
                tenantAccessService.getCurrentUserId()
        );

        Tts savedTts =
                ttsRepository.save(
                        tts
                );

        log.info(
                "TTS created successfully. Public Id : {}",
                savedTts.getPublicId()
        );

        return ttsMapper.toResponse(
                savedTts
        );
    }

    /**
     * Updates an existing TTS configuration.
     *
     * @param request update TTS request
     * @return updated TTS response
     */
    @Override
    public TtsResponse update(
            UpdateTtsRequest request) {

        log.info(
                "Updating TTS. Public Id : {}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        /*
         * Validate update request.
         */
        ttsValidator.validateForUpdate(
                request
        );

        /*
         * Retrieve the existing TTS.
         */
        Tts tts =
                ttsValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * Validate access to the existing TTS.
         *
         * TTS → Agent → Tenant
         */
        tenantAccessService.validateAgentAccess(
                tts.getAgent()
        );

        /*
         * Resolve the new/target Agent.
         */
        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        /*
         * Validate access to the target Agent as well.
         *
         * This prevents an ADMIN from moving a TTS
         * configuration into another tenant.
         */
        tenantAccessService.validateAgentAccess(
                agent
        );

        /*
         * Update editable fields.
         */
        ttsMapper.updateEntity(
                request,
                tts
        );

        tts.setAgent(
                agent
        );

        /*
         * Audit information comes from
         * the authenticated user.
         */
        tts.setUpdatedBy(
                tenantAccessService.getCurrentUserId()
        );

        Tts updatedTts =
                ttsRepository.save(
                        tts
                );

        log.info(
                "TTS updated successfully. Public Id : {}",
                updatedTts.getPublicId()
        );

        return ttsMapper.toResponse(
                updatedTts
        );
    }

    /**
     * Retrieves a TTS configuration by public ID.
     *
     * @param publicId TTS public ID
     * @return TTS response
     */
    @Override
    @Transactional(readOnly = true)
    public TtsResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching TTS. Public Id : {}",
                publicId
        );

        Tts tts =
                ttsValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant access.
         */
        tenantAccessService.validateAgentAccess(
                tts.getAgent()
        );

        return ttsMapper.toResponse(
                tts
        );
    }

    /**
     * Retrieves paginated TTS configurations.
     *
     * <p>
     * SUPER_ADMIN receives configurations across all tenants.
     * ADMIN and USER receive configurations only from their
     * current tenant.
     * </p>
     *
     * @param page page number
     * @param size page size
     * @return paginated TTS response
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TtsResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching TTS configurations. Page : {}, Size : {}",
                page,
                size
        );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        Page<Tts> result;

        if (tenantAccessService.isSuperAdmin()) {

            /*
             * SUPER_ADMIN has global access.
             */
            result =
                    ttsRepository.findByIsDeleted(
                            NOT_DELETED,
                            pageable
                    );

        } else {

            /*
             * ADMIN and USER are restricted
             * to their current tenant.
             */
            result =
                    ttsRepository
                            .findByAgent_Tenant_IdAndIsDeleted(
                                    tenantAccessService.getCurrentTenantId(),
                                    NOT_DELETED,
                                    pageable
                            );
        }

        return PageResponse
                .<TtsResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        ttsMapper::toResponse
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

    /**
     * Soft deletes a TTS configuration.
     *
     * @param publicId TTS public ID
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting TTS. Public Id : {}",
                publicId
        );

        Tts tts =
                ttsValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant deletion.
         */
        tenantAccessService.validateAgentAccess(
                tts.getAgent()
        );

        tts.markAsDeleted(
                tenantAccessService.getCurrentUserId()
        );

        ttsRepository.save(
                tts
        );

        log.info(
                "TTS deleted successfully. Public Id : {}",
                publicId
        );
    }

    /**
     * Activates a TTS configuration.
     *
     * @param publicId TTS public ID
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating TTS. Public Id : {}",
                publicId
        );

        Tts tts =
                ttsValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant activation.
         */
        tenantAccessService.validateAgentAccess(
                tts.getAgent()
        );

        tts.activate(
                tenantAccessService.getCurrentUserId()
        );

        ttsRepository.save(
                tts
        );

        log.info(
                "TTS activated successfully. Public Id : {}",
                publicId
        );
    }

    /**
     * Deactivates a TTS configuration.
     *
     * @param publicId TTS public ID
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating TTS. Public Id : {}",
                publicId
        );

        Tts tts =
                ttsValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant deactivation.
         */
        tenantAccessService.validateAgentAccess(
                tts.getAgent()
        );

        tts.deactivate(
                tenantAccessService.getCurrentUserId()
        );

        ttsRepository.save(
                tts
        );

        log.info(
                "TTS deactivated successfully. Public Id : {}",
                publicId
        );
    }
}