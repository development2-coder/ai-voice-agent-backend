package com.infinitio.aivoiceplatform.agentconfig.service.impl;

import com.infinitio.aivoiceplatform.agent.entity.Agent;
import com.infinitio.aivoiceplatform.agent.validator.AgentValidator;
import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigConstants;
import com.infinitio.aivoiceplatform.agentconfig.constant.AgentConfigMessages;
import com.infinitio.aivoiceplatform.agentconfig.dto.request.CreateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.request.UpdateAgentConfigRequest;
import com.infinitio.aivoiceplatform.agentconfig.dto.response.AgentConfigResponse;
import com.infinitio.aivoiceplatform.agentconfig.entity.AgentConfig;
import com.infinitio.aivoiceplatform.agentconfig.mapper.AgentConfigMapper;
import com.infinitio.aivoiceplatform.agentconfig.repository.AgentConfigRepository;
import com.infinitio.aivoiceplatform.agentconfig.service.AgentConfigService;
import com.infinitio.aivoiceplatform.agentconfig.validator.AgentConfigValidator;
import com.infinitio.aivoiceplatform.auth.service.CurrentUserService;
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for Agent Configuration.
 *
 * <p>
 * Handles Agent Configuration lifecycle operations.
 * Audit fields are populated using the currently authenticated user.
 * </p>
 *
 * @author Infinitio Digital
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AgentConfigServiceImpl
        implements AgentConfigService {

    private static final Integer NOT_DELETED = 0;

    private final AgentConfigRepository agentConfigRepository;

    private final AgentConfigMapper agentConfigMapper;

    private final AgentConfigValidator agentConfigValidator;

    private final AgentValidator agentValidator;

    private final CurrentUserService currentUserService;


    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Creates an Agent Configuration.
     *
     * <p>
     * The authenticated user is stored as createdBy.
     * </p>
     *
     * @param request create request
     * @return created Agent Configuration
     */
    @Override
    public AgentConfigResponse create(
            CreateAgentConfigRequest request) {

        log.info(
                "Creating Agent Configuration. Agent : {}",
                request != null
                        ? request.getAgentPublicId()
                        : null
        );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        agentConfigValidator.validateForCreate(
                request,
                agent.getId()
        );

        AgentConfig config =
                agentConfigMapper.toEntity(
                        request
                );

        config.setAgent(
                agent
        );

        /*
         * The UI explicitly sends ACTIVE when Create Agent
         * is clicked and DRAFT when Save Draft is clicked.
         *
         * Keep DRAFT as the backend fallback when status is
         * not provided.
         */
        config.setStatus(
                request.getStatus() == null
                        || request.getStatus().isBlank()
                        ? AgentConfigConstants.STATUS_DRAFT
                        : request.getStatus().trim().toUpperCase()
        );

        /*
         * Set audit information using the authenticated user.
         *
         * BaseEntity.createdBy is NOT nullable in the database,
         * therefore it must be populated before persistence.
         */
        Long currentUserId =
                currentUserService.getCurrentUserId();

        config.setCreatedBy(
                currentUserId
        );

        AgentConfig savedConfig =
                agentConfigRepository.save(
                        config
                );

        log.info(
                "Agent Configuration created successfully. " +
                        "publicId={}, agentPublicId={}, createdBy={}",
                savedConfig.getPublicId(),
                agent.getPublicId(),
                currentUserId
        );

        return agentConfigMapper.toResponse(
                savedConfig
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Updates an Agent Configuration.
     *
     * <p>
     * The authenticated user is stored as updatedBy.
     * </p>
     *
     * @param request update request
     * @return updated Agent Configuration
     */
    @Override
    public AgentConfigResponse update(
            UpdateAgentConfigRequest request) {

        log.info(
                "Updating Agent Configuration. Public Id : {}",
                request != null
                        ? request.getPublicId()
                        : null
        );

        AgentConfig config =
                agentConfigValidator.validateAndGet(
                        request.getPublicId()
                );

        Agent agent =
                agentValidator.validateAndGet(
                        request.getAgentPublicId()
                );

        agentConfigValidator.validateForUpdate(
                request,
                agent.getId()
        );

        agentConfigMapper.updateEntity(
                request,
                config
        );

        config.setAgent(
                agent
        );

        /*
         * The UI explicitly sends the desired configuration status.
         *
         * Update Agent -> ACTIVE
         * Save Draft   -> DRAFT
         */
        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            config.setStatus(
                    request.getStatus()
                            .trim()
                            .toUpperCase()
            );
        }

        /*
         * Preserve the original createdBy value.
         *
         * Only updatedBy should change during an update.
         */
        config.setUpdatedBy(
                currentUserService.getCurrentUserId()
        );

        AgentConfig updatedConfig =
                agentConfigRepository.save(
                        config
                );

        log.info(
                "Agent Configuration updated successfully. " +
                        "publicId={}, updatedBy={}",
                updatedConfig.getPublicId(),
                updatedConfig.getUpdatedBy()
        );

        return agentConfigMapper.toResponse(
                updatedConfig
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    /**
     * Fetches an Agent Configuration by public ID.
     *
     * @param publicId Agent Configuration public ID
     * @return Agent Configuration
     */
    @Override
    @Transactional(readOnly = true)
    public AgentConfigResponse getByPublicId(
            String publicId) {

        log.info(
                "Fetching Agent Configuration. Public Id : {}",
                publicId
        );

        AgentConfig config =
                agentConfigValidator.validateAndGet(
                        publicId
                );

        return agentConfigMapper.toResponse(
                config
        );
    }


    // =========================================================
    // GET BY AGENT
    // =========================================================

    /**
     * Fetches the configuration belonging to an Agent.
     *
     * @param agentPublicId Agent public ID
     * @return Agent Configuration
     */
    @Override
    @Transactional(readOnly = true)
    public AgentConfigResponse getByAgent(
            String agentPublicId) {

        log.info(
                "Fetching Agent Configuration. Agent : {}",
                agentPublicId
        );

        Agent agent =
                agentValidator.validateAndGet(
                        agentPublicId
                );

        AgentConfig config =
                agentConfigRepository
                        .findByAgentIdAndIsDeleted(
                                agent.getId(),
                                NOT_DELETED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        AgentConfigMessages.NOT_FOUND
                                )
                        );

        return agentConfigMapper.toResponse(
                config
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Fetches all non-deleted Agent Configurations.
     *
     * @param page page number
     * @param size page size
     * @return paginated Agent Configurations
     */
    // =========================================================
// GET ALL
// =========================================================

    /**
     * Fetches all non-deleted Agent Configurations.
     *
     * @return all Agent Configurations
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<AgentConfigResponse> getAll() {

        log.info(
                "Fetching all Agent Configurations"
        );

        List<AgentConfig> agentConfigs =
                agentConfigRepository.findByIsDeleted(
                        NOT_DELETED
                );

        List<AgentConfigResponse> content =
                agentConfigs.stream()
                        .map(
                                agentConfigMapper::toResponse
                        )
                        .toList();

        return PageResponse
                .<AgentConfigResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(content.size())
                .totalPages(
                        content.isEmpty() ? 0 : 1
                )
                .totalElements(content.size())
                .first(true)
                .last(true)
                .build();
    }


    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Soft deletes an Agent Configuration.
     *
     * @param publicId Agent Configuration public ID
     */
    @Override
    public void delete(
            String publicId) {

        log.info(
                "Deleting Agent Configuration. Public Id : {}",
                publicId
        );

        AgentConfig config =
                agentConfigValidator.validateAndGet(
                        publicId
                );

        config.markAsDeleted(
                currentUserService.getCurrentUserId()
        );

        agentConfigRepository.save(
                config
        );

        log.info(
                "Agent Configuration deleted successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // ACTIVATE
    // =========================================================

    /**
     * Activates an Agent Configuration.
     *
     * @param publicId Agent Configuration public ID
     */
    @Override
    public void activate(
            String publicId) {

        log.info(
                "Activating Agent Configuration. Public Id : {}",
                publicId
        );

        AgentConfig config =
                agentConfigValidator.validateAndGet(
                        publicId
                );

        config.activate(
                currentUserService.getCurrentUserId()
        );

        config.setStatus(
                AgentConfigConstants.STATUS_ACTIVE
        );

        agentConfigRepository.save(
                config
        );

        log.info(
                "Agent Configuration activated successfully. Public Id : {}",
                publicId
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    /**
     * Deactivates an Agent Configuration.
     *
     * @param publicId Agent Configuration public ID
     */
    @Override
    public void deactivate(
            String publicId) {

        log.info(
                "Deactivating Agent Configuration. Public Id : {}",
                publicId
        );

        AgentConfig config =
                agentConfigValidator.validateAndGet(
                        publicId
                );

        config.deactivate(
                currentUserService.getCurrentUserId()
        );

        config.setStatus(
                AgentConfigConstants.STATUS_INACTIVE
        );

        agentConfigRepository.save(
                config
        );

        log.info(
                "Agent Configuration deactivated successfully. Public Id : {}",
                publicId
        );
    }
}