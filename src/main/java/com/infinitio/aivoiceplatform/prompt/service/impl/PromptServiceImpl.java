package com.infinitio.aivoiceplatform.prompt.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.prompt.dto.request.CreatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.request.UpdatePromptRequest;
import com.infinitio.aivoiceplatform.prompt.dto.response.PromptResponse;
import com.infinitio.aivoiceplatform.prompt.entity.Prompt;
import com.infinitio.aivoiceplatform.prompt.mapper.PromptMapper;
import com.infinitio.aivoiceplatform.prompt.repository.PromptRepository;
import com.infinitio.aivoiceplatform.prompt.service.PromptService;
import com.infinitio.aivoiceplatform.prompt.validator.PromptValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.NOT_DELETED;

/**
 * Service implementation for Prompt.
 *
 * <p>
 * Handles Prompt CRUD operations together with
 * RBAC-compatible tenant isolation and audit handling.
 * </p>
 *
 * <p>
 * Tenant relationship:
 *
 * <pre>
 * Prompt
 *   ↓
 * Agent
 *   ↓
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
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;

    private final PromptMapper promptMapper;

    private final PromptValidator promptValidator;

    private final AgentValidator agentValidator;

    private final TenantAccessService tenantAccessService;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Creates a Prompt.
     *
     * @param request create prompt request
     * @return created prompt
     */
    @Override
    public PromptResponse create(
            CreatePromptRequest request) {

        log.info(
                "Creating Prompt. Prompt Code : {}",
                request.getPromptCode()
        );

        /*
         * Validate Prompt code/name uniqueness.
         */
        promptValidator.validateForCreate(
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
         * Validate Agent tenant access.
         *
         * SUPER_ADMIN can access any tenant.
         * ADMIN and USER can access only their own tenant.
         */
        tenantAccessService.validateAgentAccess(
                agent
        );

        Prompt prompt =
                promptMapper.toEntity(
                        request
                );

        prompt.setAgent(
                agent
        );

        /*
         * Set audit information.
         */
        prompt.setCreatedBy(
                tenantAccessService.getCurrentUserId()
        );

        Prompt savedPrompt =
                promptRepository.save(
                        prompt
                );

        log.info(
                "Prompt created successfully. " +
                        "Public Id : {}, Created By : {}",
                savedPrompt.getPublicId(),
                savedPrompt.getCreatedBy()
        );

        return promptMapper.toResponse(
                savedPrompt
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Updates a Prompt.
     *
     * @param request update prompt request
     * @return updated prompt
     */
    @Override
    public PromptResponse update(
            UpdatePromptRequest request) {

        log.info(
                "Updating Prompt. Public Id : {}",
                request.getPublicId()
        );

        /*
         * Validate uniqueness rules.
         */
        promptValidator.validateForUpdate(
                request
        );

        /*
         * Retrieve existing Prompt.
         */
        Prompt prompt =
                promptValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * Validate access to the existing Prompt.
         *
         * Prompt → Agent → Tenant
         */
        tenantAccessService.validateAgentAccess(
                prompt.getAgent()
        );

        /*
         * Resolve the target Agent.
         */
        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        /*
         * Validate target Agent as well.
         *
         * This prevents an ADMIN from moving a Prompt
         * into another tenant.
         */
        tenantAccessService.validateAgentAccess(
                agent
        );

        /*
         * Update editable fields.
         */
        promptMapper.updateEntity(
                request,
                prompt
        );

        prompt.setAgent(
                agent
        );

        /*
         * Update audit information.
         */
        prompt.setUpdatedBy(
                tenantAccessService.getCurrentUserId()
        );

        Prompt updatedPrompt =
                promptRepository.save(
                        prompt
                );

        log.info(
                "Prompt updated successfully. " +
                        "Public Id : {}, Updated By : {}",
                updatedPrompt.getPublicId(),
                updatedPrompt.getUpdatedBy()
        );

        return promptMapper.toResponse(
                updatedPrompt
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Fetches a Prompt by public ID.
     *
     * @param publicId prompt public ID
     * @return prompt
     */
    @Override
    @Transactional(readOnly = true)
    public PromptResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Prompt. Public Id : {}",
                publicId
        );

        Prompt prompt =
                promptValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant access.
         */
        tenantAccessService.validateAgentAccess(
                prompt.getAgent()
        );

        return promptMapper.toResponse(
                prompt
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Fetches Prompts according to the current user's tenant.
     *
     * <p>
     * SUPER_ADMIN receives Prompts across all tenants.
     * ADMIN and USER receive only Prompts belonging to
     * their current tenant.
     * </p>
     *
     * @param page page number
     * @param size page size
     * @return paginated prompts
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromptResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching Prompts. Page : {}, Size : {}",
                page,
                size
        );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        Page<Prompt> result;

        if (tenantAccessService.isSuperAdmin()) {

            /*
             * SUPER_ADMIN has global access.
             */
            result =
                    promptRepository.findByIsDeleted(
                            NOT_DELETED,
                            pageable
                    );

        } else {

            /*
             * ADMIN and USER are restricted
             * to their current tenant.
             */
            result =
                    promptRepository
                            .findByAgent_Tenant_IdAndIsDeleted(
                                    tenantAccessService.getCurrentTenantId(),
                                    NOT_DELETED,
                                    pageable
                            );
        }

        return PageResponse
                .<PromptResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        promptMapper::toResponse
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

    /**
     * Soft deletes a Prompt.
     *
     * @param publicId prompt public ID
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Prompt. Public Id : {}",
                publicId
        );

        Prompt prompt =
                promptValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant deletion.
         */
        tenantAccessService.validateAgentAccess(
                prompt.getAgent()
        );

        prompt.markAsDeleted(
                tenantAccessService.getCurrentUserId()
        );

        promptRepository.save(
                prompt
        );

        log.info(
                "Prompt deleted successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activates a Prompt.
     *
     * @param publicId prompt public ID
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Prompt. Public Id : {}",
                publicId
        );

        Prompt prompt =
                promptValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant activation.
         */
        tenantAccessService.validateAgentAccess(
                prompt.getAgent()
        );

        prompt.activate(
                tenantAccessService.getCurrentUserId()
        );

        promptRepository.save(
                prompt
        );

        log.info(
                "Prompt activated successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivates a Prompt.
     *
     * @param publicId prompt public ID
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Prompt. Public Id : {}",
                publicId
        );

        Prompt prompt =
                promptValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant deactivation.
         */
        tenantAccessService.validateAgentAccess(
                prompt.getAgent()
        );

        prompt.deactivate(
                tenantAccessService.getCurrentUserId()
        );

        promptRepository.save(
                prompt
        );

        log.info(
                "Prompt deactivated successfully. Public Id : {}",
                publicId
        );
    }
}