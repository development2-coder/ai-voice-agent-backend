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
import com.infinitio.aivoiceplatform.common.dto.PageResponse;
import com.infinitio.aivoiceplatform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Agent Configuration.
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

    private static final Long SYSTEM_USER_ID = 1L;

    private final AgentConfigRepository agentConfigRepository;

    private final AgentConfigMapper agentConfigMapper;

    private final AgentConfigValidator agentConfigValidator;

    private final AgentValidator agentValidator;


    // =========================================================
    // CREATE
    // =========================================================

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

        AgentConfig savedConfig =
                agentConfigRepository.save(
                        config
                );

        log.info(
                "Agent Configuration created successfully. Public Id : {}",
                savedConfig.getPublicId()
        );

        return agentConfigMapper.toResponse(
                savedConfig
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

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

        AgentConfig updatedConfig =
                agentConfigRepository.save(
                        config
                );

        log.info(
                "Agent Configuration updated successfully. Public Id : {}",
                updatedConfig.getPublicId()
        );

        return agentConfigMapper.toResponse(
                updatedConfig
        );
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AgentConfigResponse> getAll(
            int page,
            int size) {

        agentConfigValidator.validatePagination(
                page,
                size
        );

        log.info(
                "Fetching Agent Configurations. Page : {}, Size : {}",
                page,
                size
        );

        Page<AgentConfig> result =
                agentConfigRepository.findByIsDeleted(
                        NOT_DELETED,
                        PageRequest.of(
                                page,
                                size
                        )
                );

        return PageResponse
                .<AgentConfigResponse>builder()
                .content(
                        result.getContent()
                                .stream()
                                .map(
                                        agentConfigMapper::toResponse
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
                "Deleting Agent Configuration. Public Id : {}",
                publicId
        );

        AgentConfig config =
                agentConfigValidator.validateAndGet(
                        publicId
                );

        config.markAsDeleted(
                SYSTEM_USER_ID
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
                SYSTEM_USER_ID
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
                SYSTEM_USER_ID
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