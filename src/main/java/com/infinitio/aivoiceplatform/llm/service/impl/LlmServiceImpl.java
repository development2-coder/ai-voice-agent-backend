package com.infinitio.aivoiceplatform.llm.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.auth.service.TenantAccessService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.llm.dto.request.CreateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.request.UpdateLlmRequest;
import com.infinitio.aivoiceplatform.llm.dto.response.LlmResponse;
import com.infinitio.aivoiceplatform.llm.entity.Llm;
import com.infinitio.aivoiceplatform.llm.mapper.LlmMapper;
import com.infinitio.aivoiceplatform.llm.repository.LlmRepository;
import com.infinitio.aivoiceplatform.llm.service.LlmService;
import com.infinitio.aivoiceplatform.llm.validator.LlmValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.infinitio.aivoiceplatform.common.constants.ApplicationConstants.NOT_DELETED;

/**
 * Service implementation for LLM.
 *
 * <p>
 * Handles LLM configuration CRUD operations along with
 * tenant isolation and current-user audit handling.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LlmServiceImpl implements LlmService {

    private final LlmRepository llmRepository;

    private final LlmMapper llmMapper;

    private final LlmValidator llmValidator;

    private final AgentValidator agentValidator;

    private final TenantAccessService tenantAccessService;

    /**
     * Creates a new LLM configuration.
     *
     * @param request create LLM request
     * @return created LLM response
     */
    @Override
    public LlmResponse create(CreateLlmRequest request) {

        log.info(
                "Creating LLM. Code : {}, Provider : {}, Model : {}",
                request.getLlmCode(),
                request.getProvider(),
                request.getModel()
        );

        llmValidator.validateForCreate(request);

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        /*
         * Prevent ADMIN/USER from creating an LLM
         * under another tenant's Agent.
         */
        tenantAccessService.validateAgentAccess(agent);

        Llm llm =
                llmMapper.toEntity(request);

        llm.setAgent(agent);

        /*
         * Set the actual authenticated user as creator.
         */
        llm.setCreatedBy(
                tenantAccessService.getCurrentUserId()
        );

        Llm savedLlm =
                llmRepository.save(llm);

        log.info(
                "LLM created successfully. Public Id : {}",
                savedLlm.getPublicId()
        );

        return llmMapper.toResponse(savedLlm);
    }

    /**
     * Updates an existing LLM configuration.
     *
     * @param request update LLM request
     * @return updated LLM response
     */
    @Override
    public LlmResponse update(UpdateLlmRequest request) {

        log.info(
                "Updating LLM. Public Id : {}",
                request.getPublicId()
        );

        llmValidator.validateForUpdate(request);

        Llm llm =
                llmValidator.validateAndGet(
                        request.getPublicId()
                );

        /*
         * Validate access to the existing LLM.
         */
        tenantAccessService.validateAgentAccess(
                llm.getAgent()
        );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        /*
         * Validate access to the new/target Agent as well.
         *
         * This prevents an ADMIN from moving an LLM
         * from one tenant to another tenant.
         */
        tenantAccessService.validateAgentAccess(agent);

        llmMapper.updateEntity(
                request,
                llm
        );

        llm.setAgent(agent);

        Llm updatedLlm =
                llmRepository.save(llm);

        log.info(
                "LLM updated successfully. Public Id : {}",
                updatedLlm.getPublicId()
        );

        return llmMapper.toResponse(updatedLlm);
    }

    /**
     * Retrieves an LLM by public ID.
     *
     * @param publicId LLM public ID
     * @return LLM response
     */
    @Override
    @Transactional(readOnly = true)
    public LlmResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching LLM. Public Id : {}",
                publicId
        );

        Llm llm =
                llmValidator.validateAndGet(
                        publicId
                );

        /*
         * Validate tenant ownership before returning
         * the LLM configuration.
         */
        tenantAccessService.validateAgentAccess(
                llm.getAgent()
        );

        return llmMapper.toResponse(llm);
    }

    /**
     * Retrieves LLM configurations.
     *
     * <p>
     * SUPER_ADMIN can access LLM configurations across
     * all tenants. ADMIN and USER can access only
     * configurations belonging to their current tenant.
     * </p>
     *
     * @param page page number
     * @param size page size
     * @return paginated LLM response
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<LlmResponse> getAll(
            int page,
            int size) {

        log.info(
                "Fetching LLM configurations. Page : {}, Size : {}",
                page,
                size
        );

        Pageable pageable =
                PageRequest.of(page, size);

        Page<Llm> result;

        if (tenantAccessService.isSuperAdmin()) {

            /*
             * SUPER_ADMIN has global access.
             */
            result =
                    llmRepository.findByIsDeleted(
                            NOT_DELETED,
                            pageable
                    );

        } else {

            /*
             * ADMIN and USER can access only
             * their own tenant's LLM configurations.
             */
            result =
                    llmRepository
                            .findByAgent_Tenant_IdAndIsDeleted(
                                    tenantAccessService.getCurrentTenantId(),
                                    NOT_DELETED,
                                    pageable
                            );
        }

        return PageResponse.<LlmResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(llmMapper::toResponse)
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
     * Soft deletes an LLM configuration.
     *
     * @param publicId LLM public ID
     */
    @Override
    public void delete(String publicId) {

        log.info(
                "Deleting LLM. Public Id : {}",
                publicId
        );

        Llm llm =
                llmValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant deletion.
         */
        tenantAccessService.validateAgentAccess(
                llm.getAgent()
        );

        llm.markAsDeleted(
                tenantAccessService.getCurrentUserId()
        );

        llmRepository.save(llm);

        log.info(
                "LLM deleted successfully. Public Id : {}",
                publicId
        );
    }

    /**
     * Activates an LLM configuration.
     *
     * @param publicId LLM public ID
     */
    @Override
    public void activate(String publicId) {

        log.info(
                "Activating LLM. Public Id : {}",
                publicId
        );

        Llm llm =
                llmValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant activation.
         */
        tenantAccessService.validateAgentAccess(
                llm.getAgent()
        );

        llm.activate(
                tenantAccessService.getCurrentUserId()
        );

        llmRepository.save(llm);

        log.info(
                "LLM activated successfully. Public Id : {}",
                publicId
        );
    }

    /**
     * Deactivates an LLM configuration.
     *
     * @param publicId LLM public ID
     */
    @Override
    public void deactivate(String publicId) {

        log.info(
                "Deactivating LLM. Public Id : {}",
                publicId
        );

        Llm llm =
                llmValidator.validateAndGet(
                        publicId
                );

        /*
         * Prevent cross-tenant deactivation.
         */
        tenantAccessService.validateAgentAccess(
                llm.getAgent()
        );

        llm.deactivate(
                tenantAccessService.getCurrentUserId()
        );

        llmRepository.save(llm);

        log.info(
                "LLM deactivated successfully. Public Id : {}",
                publicId
        );
    }
}